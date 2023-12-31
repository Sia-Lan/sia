package org.sia.framework;

import java.security.InvalidParameterException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: Sia.Lan
 *
 * @create: 2023-06-30
 */
@Slf4j
public class TaskPipelineManager {

    static {
        TaskWatcher.scheduleWatch();
    }

    private static final Map<String, ReentrantLock> LOCK_NAME_LOCK_PAIR = new ConcurrentHashMap<>();
    private static final Map<ReentrantLock, TaskNodeContainer<FutureTask<?>>> LOCK_TASK_PAIR =
            new ConcurrentHashMap<>();

    /**
     * 当任务节点出错时串行子任务不中断，继续执行。
     *
     * @param taskNamespace
     * @param pipelineId
     * @param task
     * @param executorService
     * @param <V>
     */
    public static <V> void submitUninterruptedPipelineTask(String taskNamespace, String pipelineId,
                                                           FutureTask<V> task, ExecutorService executorService) {
        submit(taskNamespace, pipelineId, task, executorService, false);
    }

    /**
     * 当任务节点出错时串行子任务被中断。
     *
     * @param taskNamespace
     * @param pipelineId
     * @param task
     * @param executorService
     */
    public static <V> void submitInterruptedPipelineTask(String taskNamespace, String pipelineId,
                                                         FutureTask<V> task, ExecutorService executorService) {
        submit(taskNamespace, pipelineId, task, executorService, true);
    }

    /**
     * 这是一个以taskNamespace + pipelineId为分组条件的高性能异步并行执行框架。</br>
     * 把提交进来的任务，按照提交顺序，通过taskNamespace + pipelineId进行分组，值相同的串行执行，值不同的并行执行。</br>
     *
     * @param taskNamespace
     * @param pipelineId
     * @param task
     * @param executorService
     * @param interruptedSubtask
     */
    private static <V> void submit(String taskNamespace, String pipelineId, FutureTask<V> task,
                                   ExecutorService executorService, boolean interruptedSubtask) {
        if (StringUtils.isEmpty(taskNamespace)) {
            throw new InvalidParameterException("task namespace nonnull.");
        }
        if (StringUtils.isEmpty(pipelineId)) {
            throw new InvalidParameterException("pipeline ID nonnull.");
        }
        if (Objects.isNull(task)) {
            throw new InvalidParameterException("task nonnull.");
        }
        if (Objects.isNull(executorService)) {
            throw new InvalidParameterException("executor service nonnull.");
        }
        String lockName = generateLockName(taskNamespace, pipelineId);
        createOrAppend(lockName, task, executorService, interruptedSubtask);
    }

    private static <V> void createOrAppend(String lockName, FutureTask<V> task,
                                           ExecutorService executorService, boolean interruptedSubtask) {

        if (StringUtils.isEmpty(lockName)) {
            throw new InvalidParameterException("lock name nonnull.");
        }
        ReentrantLock lock = LOCK_NAME_LOCK_PAIR.get(lockName);
        try {
            boolean shouldCreateLock = false;
            if (Objects.isNull(lock)) {
                shouldCreateLock = true;
            } else {
                lock.lock();
                if (!LOCK_NAME_LOCK_PAIR.containsKey(lockName)) {
                    shouldCreateLock = true;
                    lock.unlock();
                }
            }
            if (shouldCreateLock) {
                synchronized (TaskPipelineManager.class) {
                    if (LOCK_NAME_LOCK_PAIR.containsKey(lockName)) {
                        lock = LOCK_NAME_LOCK_PAIR.get(lockName);
                    } else {
                        // 公平锁，按提交的顺序进行锁定
                        lock = new ReentrantLock(true);
                        LOCK_NAME_LOCK_PAIR.put(lockName, lock);
                    }
                    lock.lock();
                }
            }
            if (!LOCK_TASK_PAIR.containsKey(lock)) {
                TaskNodeContainer<FutureTask<?>> taskNodeContainer =
                        TaskNodeContainer.create(TaskNode.create(task, interruptedSubtask));
                LOCK_TASK_PAIR.put(lock, taskNodeContainer);
                TaskPipelineRunnable taskPipelineRunnable =
                        TaskPipelineRunnable.create(lockName, lock, taskNodeContainer);
                executorService.submit(taskPipelineRunnable);
            } else {
                TaskNode<FutureTask<?>> current = LOCK_TASK_PAIR.get(lock).getHead();
                while (Objects.nonNull(current.next())) {
                    current = current.next();
                }
                current.append(TaskNode.create(task, interruptedSubtask));
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * 资源回收
     *
     * @param lockName
     */
    protected static void resourceRecovery(String lockName) {
        synchronized (TaskPipelineManager.class) {
            ReentrantLock lock = LOCK_NAME_LOCK_PAIR.get(lockName);
            try {
                lock.lock();
                LOCK_TASK_PAIR.remove(lock);
                LOCK_NAME_LOCK_PAIR.remove(lockName);
            } finally {
                lock.unlock();
            }
        }
    }

    public static Optional<TaskView> getTaskView() {
        synchronized (TaskView.class) {
            return Optional.ofNullable(TaskView.getInstance());
        }
    }

    public static Optional<TaskView> getTaskView(String taskNamespace) {
        Map<String, Integer> taskView;
        synchronized (TaskView.class) {
            taskView = TaskView.getInstance().getTaskView();
        }
        Set<String> keySet = taskView.keySet();
        Map<String, Integer> namespaceTaskView = new HashMap<>();
        for (String key : keySet) {
            if (key.startsWith(taskNamespace)) {
                namespaceTaskView.put(key, taskView.get(key));
            }
        }
        int total = CollectionUtils.emptyIfNull(namespaceTaskView.values()).stream().reduce(0, Integer::sum);
        TaskView tv = new TaskView();
        tv.setTotal(total);
        tv.setTaskView(namespaceTaskView);
        return Optional.of(tv);
    }

    public static Optional<TaskView> getTaskView(String taskNamespace, String pipelineId) {
        Map<String, Integer> taskView;
        synchronized (TaskView.class) {
            taskView = TaskView.getInstance().getTaskView();
        }
        String lockName = generateLockName(taskNamespace, pipelineId);
        int count = taskView.getOrDefault(lockName, 0);
        Map<String, Integer> lockNameTaskView = new HashMap<>();
        lockNameTaskView.put(lockName, count);
        TaskView tv = new TaskView();
        tv.setTotal(count);
        tv.setTaskView(lockNameTaskView);
        return Optional.of(tv);
    }

    private static String generateLockName(String taskNamespace, String pipelineId) {
        return taskNamespace + ":" + pipelineId;
    }

    @Data
    public static class TaskView {

        private int total;

        private Map<String, Integer> taskView = new HashMap<>();

        private TaskView() {}

        private static final TaskView SINGLETON = new TaskView();

        private static TaskView getInstance() {
            return SINGLETON;
        }
    }

    public static class TaskWatcher {

        static volatile AtomicBoolean isWatching = new AtomicBoolean(false);

        private TaskWatcher() {}

        public static void scheduleWatch() {
            boolean w = isWatching.get();
            if (w) {
                return;
            }
            isWatching.set(true);
            TimerTask watchingTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        Set<String> lockNameSet = LOCK_NAME_LOCK_PAIR.keySet();
                        if (CollectionUtils.isEmpty(lockNameSet)) {
                            return;
                        }
                        Iterator<String> lockNames = lockNameSet.iterator();
                        Map<String, Integer> taskView = new HashMap<>();
                        while (lockNames.hasNext()) {
                            String lockName = lockNames.next();
                            ReentrantLock lock = LOCK_NAME_LOCK_PAIR.get(lockName);
                            if (Objects.isNull(lock)) {
                                continue;
                            }
                            TaskNodeContainer<FutureTask<?>> container = LOCK_TASK_PAIR.get(lock);
                            if (Objects.isNull(container)) {
                                continue;
                            }
                            int i = 0;
                            TaskNode<?> current = container.getHead();
                            while (Objects.nonNull(current.next())) {
                                i++;
                                current = current.next();
                            }
                            taskView.put(lockName, i);
                        }
                        int total = CollectionUtils.emptyIfNull(taskView.values()).stream().reduce(0, Integer::sum);
                        synchronized (TaskView.class) {
                            TaskView.getInstance().setTotal(total);
                            TaskView.getInstance().setTaskView(taskView);
                        }
                        log.info("task view : {}", TaskView.getInstance());
                    } catch (Throwable e) {
                        log.error("task view error", e);
                    }
                }
            };
            Timer timer = new Timer("TaskPipelineView", true);
            long period = 10_000L;
            timer.schedule(watchingTask, 0, period);
        }
    }
}

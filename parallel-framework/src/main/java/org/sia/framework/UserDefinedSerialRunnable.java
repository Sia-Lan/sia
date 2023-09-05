package org.sia.framework;

import java.util.Objects;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;

/**
 * @author: Sia.Lan
 *
 * @create: 2023-06-30
 */

@Slf4j
public class UserDefinedSerialRunnable implements Runnable {

    private final String lockName;

    private final ReentrantLock lock;

    private TaskNode<FutureTask<?>> current;

    private final TaskNodeContainer<FutureTask<?>> container;

    private boolean suggestedInterrupt = false;

    private TaskNode<FutureTask<?>> previous = TaskNode.create(null, true);

    private UserDefinedSerialRunnable(String lockName, ReentrantLock lock, TaskNodeContainer<FutureTask<?>> container) {
        this.lockName = lockName;
        this.lock = lock;
        this.current = container.getHead();
        this.previous.append(current);
        this.container = container;
    }

    public static UserDefinedSerialRunnable create(String lockName, ReentrantLock lock,
                                                   TaskNodeContainer<FutureTask<?>> container) {
        return new UserDefinedSerialRunnable(lockName, lock, container);
    }

    @Override
    public void run() {
        while (Objects.nonNull(current)) {
            FutureTask<?> task = current.value();
            if (current.shouldInterrupt(suggestedInterrupt)) {
                task.cancel(false);
            } else {
                try {
                    task.run();
                    task.get();
                } catch (Throwable e) {
                    log.error("The subtask fails to be executed. lock name is {}", lockName, e);
                    suggestedInterrupt = true;
                }
            }
            avoidOutOfMemory();
            previous = previous.next();
            current = current.next();
        }
        try {
            lock.lock();
            if (Objects.nonNull(previous.next())) {
                current = previous.next();
                run();
            } else {
                UserDefinedSerialTaskManager.resourceRecovery(lockName);
            }
        } finally {
            if (Objects.nonNull(lock)) {
                lock.unlock();
            }
        }
    }

    private void avoidOutOfMemory() {
        container.avoidOutOfMemory(previous);
    }
}

package org.sia.framework;

/**
 * @author: Sia.Lan
 *
 * @create: 2023-06-30
 */
public class TaskNode<T> {

    private final T value;

    private final boolean interruptedSubtask;

    private volatile TaskNode<T> next;

    private TaskNode(T value, boolean interruptedSubtask) {
        this.value = value;
        this.interruptedSubtask = interruptedSubtask;
    }

    public static <T> TaskNode<T> create(T value, boolean interruptedSubtask) {
        return new TaskNode<>(value, interruptedSubtask);
    }

    public TaskNode<T> next() {
        return next;
    }

    public T value() {
        return value;
    }

    public void append(TaskNode<T> next) {
        this.next = next;
    }

    public boolean shouldInterrupt(boolean suggestedInterrupt) {
        return interruptedSubtask && suggestedInterrupt;
    }
}

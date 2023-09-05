package org.sia.framework;

import java.util.concurrent.FutureTask;

import lombok.Data;

/**
 * @author: Sia.Lan
 *
 * @create: 2023-06-30
 */
@Data
public class TaskNodeContainer<T> {

    private volatile TaskNode<T> head;

    private TaskNodeContainer(TaskNode<T> head) {
        this.head = head;
    }

    public static TaskNodeContainer<FutureTask<?>> create(TaskNode<FutureTask<?>> taskNode) {
        return new TaskNodeContainer<>(taskNode);
    }

    public void avoidOutOfMemory(TaskNode<T> newHead) {
        this.head = newHead;
    }
}
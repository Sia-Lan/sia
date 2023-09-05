package org.sia.framework;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

public class UserDefinedSerialTaskManagerTest {

    private static ExecutorService EXECUTOR = Executors.newFixedThreadPool(10);

    public static void main(String[] args) {
        AtomicInteger i = new AtomicInteger(0);
        while(i.getAndIncrement() <=100) {
            UserDefinedSerialTaskManager.submitUninterruptedSerialTask("taskNamespace-A", "userId-J", new FutureTask<>(runnable("taskNamespace-A:userId-J" + i.get()), "over"), EXECUTOR);
            UserDefinedSerialTaskManager.submitUninterruptedSerialTask("taskNamespace-A", "userId-K", new FutureTask<>(runnable("taskNamespace-A:userId-K" + i.get()), "over"), EXECUTOR);
            UserDefinedSerialTaskManager.submitUninterruptedSerialTask("taskNamespace-A", "userId-L", new FutureTask<>(runnable("taskNamespace-A:userId-L" + i.get()), "over"), EXECUTOR);
        }
        EXECUTOR.shutdown();
    }

    static Runnable runnable(String str) {
        return () -> System.out.println(str);
    }
}

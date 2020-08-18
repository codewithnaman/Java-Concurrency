package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolRunnableDifferentMethods {

    public static void task() {
        System.out.println("Running the task in Thread : " + Thread.currentThread());
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Task Ended in Thread : " + Thread.currentThread());
    }

    public static void main(String[] args) throws InterruptedException {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        System.out.println("Sending task");
        pool.execute(()->task());
        pool.execute(ForkJoinPoolRunnableDifferentMethods::task);
        ForkJoinTask task = ForkJoinTask.adapt(ForkJoinPoolRunnableDifferentMethods::task);
        pool.execute(task);
        System.out.println("Done");
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

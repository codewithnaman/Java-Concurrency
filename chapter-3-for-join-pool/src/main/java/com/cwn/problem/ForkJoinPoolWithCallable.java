package com.cwn.problem;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolWithCallable {

    public static int compute() {
        System.out.println("Starting compute in Thread : " + Thread.currentThread());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("compute Completed in Thread : " + Thread.currentThread());
        return new Random().nextInt();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("Start");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ForkJoinTask<Integer> task = ForkJoinTask.adapt(ForkJoinPoolWithCallable::compute);
        Integer result = pool.invoke(task);
        System.out.println("Done " + result);
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

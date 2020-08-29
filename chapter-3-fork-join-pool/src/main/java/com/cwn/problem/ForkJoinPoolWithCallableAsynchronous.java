package com.cwn.problem;

import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolWithCallableAsynchronous {

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

    public static void main(String[] args) throws Exception {
        System.out.println("Start");
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ForkJoinTask<Integer> result = pool.submit(ForkJoinPoolWithCallableAsynchronous::compute);
        System.out.println("Doing some other work");
        Thread.sleep(2000);
        System.out.println("Other Work Done");
        System.out.println("First task Done " + result.get());

        ForkJoinTask<Integer> task = ForkJoinTask.adapt(ForkJoinPoolWithCallableAsynchronous::compute);
        ForkJoinTask<Integer> result2 = pool.submit(task);
        System.out.println("Doing some other work");
        Thread.sleep(2000);
        System.out.println("Other Work Done");
        System.out.println("Second task Done " + result2.get());
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }

}

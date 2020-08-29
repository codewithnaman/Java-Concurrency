package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;

public class ForkJoinPoolWithRunnable {

    public static void main(String[] args) {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        //ForkJoinPool pool = new ForkJoinPool(100);
        System.out.println(pool);
        for (int i = 0; i < 40; i++) {
            int index = i;
            pool.submit(() -> task(index));
        }
        while (pool.getStealCount() != 40) {
            System.out.println(pool);
            sleep(500);
        }
    }

    private static void task(int id) {
        System.out.println("Started: " + id + " " + Thread.currentThread());
        sleep(3000);
        System.out.println("Completed: " + id + " " + Thread.currentThread());
    }

    private static void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

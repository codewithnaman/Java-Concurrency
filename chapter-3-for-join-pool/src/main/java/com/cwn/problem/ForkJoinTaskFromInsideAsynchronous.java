package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinTaskFromInsideAsynchronous {

    public static int divideAndConquerProblem(int number) {
        System.out.println("Number "+number+" is Processing by Thread "+Thread.currentThread());
        if (number <= 1) {
            return 1;
        } else {
            int subtaskNumber = number / 2;

            ForkJoinTask<Integer> task1 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            ForkJoinTask<Integer> task2 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));

            ForkJoinTask<Integer> result1 = task1.fork();
            ForkJoinTask<Integer> result2 = task2.fork();
            return result1.join() + result2.join();
        }

    }

    public static void main(String[] args) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(50);
        pool.submit(()-> System.out.println(divideAndConquerProblem(6)));
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

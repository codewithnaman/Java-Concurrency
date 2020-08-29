package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ExecutorServiceInducedDeadLockSolvedUsingForkJoinPool {
    private static ForkJoinPool pool;

    private static boolean isPrime(int number) {
        boolean isDivisible = false;
        for (int i = 2; i < number; i++) {
            if (number % i == 0) {
                isDivisible = true;
                break;
            }
        }
        return number > 1 && !isDivisible;
    }

    private static int numberOfPrimeNumbersInRange(int start, int end) {
        int count = 0;
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) count++;
        }
        return count;
    }

    public static int splitAndCompute(int start, int end) throws Exception {
        System.out.println("Computing Prime number from " + start + " to " + end + " " + Thread.currentThread());
        int difference = end - start;
        int result;
        if (difference < 100) {
            result = numberOfPrimeNumbersInRange(start, end);
        } else {
            int middle = start + difference / 2;
            ForkJoinTask<Integer> task1 = ForkJoinTask.adapt(() -> splitAndCompute(start, middle)).fork();
            ForkJoinTask<Integer> task2 = ForkJoinTask.adapt(() -> splitAndCompute(middle + 1, end)).fork();
            result = task1.join() + task2.join();
        }
        System.out.println("Computing Prime number from " + start + " to " + end + " Completed " + Thread.currentThread());
        return result;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("System Available Cores " + Runtime.getRuntime().availableProcessors());
        int threadPoolSize = 100;
        pool = new ForkJoinPool(threadPoolSize);
        int start = 1;
        int end = 100000;
        System.out.println("Prime numbers between Start : " + start + " End : " + end + " is "
                + pool.invoke(ForkJoinTask.adapt(()->splitAndCompute(start, end))));
        pool.shutdown();

    }
}

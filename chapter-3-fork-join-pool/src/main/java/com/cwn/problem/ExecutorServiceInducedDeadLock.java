package com.cwn.problem;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ExecutorServiceInducedDeadLock {

    private static ExecutorService pool;

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
            Future<Integer> task1 = pool.submit(() -> splitAndCompute(start, middle));
            Future<Integer> task2 = pool.submit(() -> splitAndCompute(middle + 1, end));
            result = task1.get() + task2.get();
        }
        System.out.println("Computing Prime number from " + start + " to " + end + " Completed " + Thread.currentThread());
        return result;
    }


    public static void main(String[] args) throws Exception {
        System.out.println("System Available Cores " + Runtime.getRuntime().availableProcessors());
        int threadPoolSize = 100;
        pool = Executors.newFixedThreadPool(threadPoolSize);
        int start = 1;
        int end = 1000;
        System.out.println("Prime numbers between Start : " + start + " End : " + end + " is "
                + splitAndCompute(start, end));
        pool.shutdown();

    }
}

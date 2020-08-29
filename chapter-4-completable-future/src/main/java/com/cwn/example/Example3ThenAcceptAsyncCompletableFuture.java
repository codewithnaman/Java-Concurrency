package com.cwn.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

public class Example3ThenAcceptAsyncCompletableFuture {

    public static int supplierFunction() {
        sleep(1000);
        System.out.println("Generating Value " + Thread.currentThread());
        return 5;
    }

    public static void consumer(int number) {
        System.out.println("Printing the Number " + Thread.currentThread());
        System.out.println("Number is " + number);
    }

    public static void main(String[] args) {
        ForkJoinPool taskPool = new ForkJoinPool(10);
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example3ThenAcceptAsyncCompletableFuture::supplierFunction,taskPool);
        ForkJoinPool consumerPool = new ForkJoinPool(10);
        future.thenAcceptAsync(Example3ThenAcceptAsyncCompletableFuture::consumer,consumerPool);
        sleep(4000);
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

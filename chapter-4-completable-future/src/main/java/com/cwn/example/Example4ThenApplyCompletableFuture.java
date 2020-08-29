package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example4ThenApplyCompletableFuture {

    public static int supplierFunction() {
        System.out.println("Generating Value " + Thread.currentThread());
        return 5;
    }

    public static void consumer(int number) {
        System.out.println("Printing the Number " + Thread.currentThread());
        System.out.println("Number is " + number);
    }

    public static int transform(int number) {
        System.out.println("Doubling Number " + Thread.currentThread());
        return number * 2;
    }

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(Example4ThenApplyCompletableFuture::supplierFunction)
                .thenApply(Example4ThenApplyCompletableFuture::transform)
                .thenAcceptAsync(Example4ThenApplyCompletableFuture::consumer);
        sleep(4000);
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

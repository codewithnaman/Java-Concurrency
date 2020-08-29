package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example2ThenAcceptCompletableFuture {

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
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example2ThenAcceptCompletableFuture::supplierFunction);
        System.out.println("Registering then Accept");
        future.thenAccept(Example2ThenAcceptCompletableFuture::consumer);
        System.out.println("Main Almost Completed");
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

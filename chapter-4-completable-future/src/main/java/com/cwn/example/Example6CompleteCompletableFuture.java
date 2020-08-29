package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example6CompleteCompletableFuture {

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

    public static void applyChainOfOperations(CompletableFuture<Integer> future) {
        System.out.println(future);
        future.thenApply(Example6CompleteCompletableFuture::transform)
                .thenAcceptAsync(Example6CompleteCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
    }

    public static void main(String[] args) {
      //  CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example6CompleteCompletableFuture::supplierFunction);
       // sleep(1000);
      //  applyChainOfOperations(future);
        CompletableFuture<Integer> test = new CompletableFuture<>();
        applyChainOfOperations(test);
        System.out.println(test);
        test.complete(6);
        System.out.println(test);
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

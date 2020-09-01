package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example7ExceptionCompletableFuture {

    private static int i = 1;

    public static int supplierFunction() {
        System.out.println("Generating Value " + Thread.currentThread());
        if (i % 2 == 0) {
            throw new RuntimeException("Something went wrong");
        }
        return i++;
    }

    public static void consumer(int number) {
        System.out.println("Printing the Number " + Thread.currentThread());
        System.out.println("Number is " + number);
    }

    public static int transform(int number) {
        System.out.println("Doubling Number " + number + " " + Thread.currentThread());
        if (number > 9) {
            throw new RuntimeException("Number is greater than 9; can't process");
        }
        return number * 2;
    }

    public static int reportError(Throwable throwable) {
        //throwable.printStackTrace();
        // throw new RuntimeException(throwable.getMessage());
        return 10;
    }

    public static int reportTransformError(Throwable throwable) {
        throwable.printStackTrace();
        throw new RuntimeException("Something went wrong");
    }

    public static int chainExceptions(Throwable throwable) {
        System.out.println("Chained messages is " + throwable.getMessage());
        throw new RuntimeException("Something went wrong");
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future.thenApply(Example7ExceptionCompletableFuture::transform)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example7ExceptionCompletableFuture::supplierFunction);
        future1.exceptionally(Example7ExceptionCompletableFuture::reportError)
                .thenApply(Example7ExceptionCompletableFuture::transform)
                .exceptionally(Example7ExceptionCompletableFuture::reportTransformError)
                .exceptionally(Example7ExceptionCompletableFuture::chainExceptions)
                .thenAcceptAsync(Example7ExceptionCompletableFuture::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));

        sleep(5000);
    }


    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

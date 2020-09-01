package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example8ExceptionCompletableFutureState {
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
        System.out.println("Exception happened with message "+ throwable.getMessage());
        throw new RuntimeException(throwable.getMessage());
    }

    public static void printStateOfFuture(CompletableFuture<Integer> completableFuture) {
        System.out.println("Is task Completed               : " + completableFuture.isDone());
        System.out.println("Is task Cancelled               : " + completableFuture.isCancelled());
        System.out.println("Is task Completed Exceptionally : " + completableFuture.isCompletedExceptionally());
    }

    public static void main(String[] args) {
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(Example8ExceptionCompletableFutureState::supplierFunction);
        future.thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        printStateOfFuture(future);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(Example8ExceptionCompletableFutureState::supplierFunction);
        future1.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        printStateOfFuture(future1);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        future2.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        future2.completeExceptionally(new RuntimeException("Task Can't performed; due to some problem"));
        printStateOfFuture(future2);
        System.out.println("----------------------------------------");
        CompletableFuture<Integer> future3 = new CompletableFuture<>();
        future3.exceptionally(Example8ExceptionCompletableFutureState::reportError)
                .thenApply(Example8ExceptionCompletableFutureState::transform)
                .thenAcceptAsync(Example8ExceptionCompletableFutureState::consumer)
                .thenRun(() -> System.out.println("All Tasks Completed"));
        future3.cancel(true);
        printStateOfFuture(future3);
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

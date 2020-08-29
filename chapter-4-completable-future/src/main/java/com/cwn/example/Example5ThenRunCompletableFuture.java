package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example5ThenRunCompletableFuture {

    public static void main(String[] args) {
        CompletableFuture.supplyAsync(Example4ThenApplyCompletableFuture::supplierFunction)
                .thenApply(Example4ThenApplyCompletableFuture::transform)
                .thenAcceptAsync(Example4ThenApplyCompletableFuture::consumer)
                .thenRun(()-> System.out.println("All Tasks Completed"));
        Example4ThenApplyCompletableFuture.sleep(4000);
    }
}

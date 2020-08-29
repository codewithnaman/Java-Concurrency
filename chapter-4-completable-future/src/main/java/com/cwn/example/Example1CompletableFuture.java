package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example1CompletableFuture {

    public static int task() {
        System.out.println("Task Started..." + Thread.currentThread());
        sleep(1000);
        System.out.println("Task Completed..." + Thread.currentThread());
        return 1;
    }

    public static void main(String[] args) throws Exception{
      /*  CompletableFuture.runAsync(() -> task());
        sleep(2000);
        System.out.println("Main completed");*/

        CompletableFuture<Integer> result = CompletableFuture.supplyAsync(() -> task());
        System.out.println("Main Completed");
        System.out.println("Result from the task is " + result.getNow(-1));
    }

    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

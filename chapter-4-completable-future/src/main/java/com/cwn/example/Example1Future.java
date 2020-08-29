package com.cwn.example;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Example1Future {

    public static int task() {
        System.out.println("Task Started...");
        sleep(1000);
        System.out.println("Task Completed...");
        return 1;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        Future<Integer> taskResult = executorService.submit(Example1Future::task);
        System.out.println("Some other instructions performed");
        int result = taskResult.get();
        System.out.println("Task Result After execution " + result);
        executorService.shutdown();
    }


    private static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

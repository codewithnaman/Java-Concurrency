package com.cwn.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Example2Future {

    public static int task(int max, int index) {
        System.out.println("Task Started..." + index);
        sleep((max - index) * 1000);
      /*  if (resultNumber > 1000) {
            throw new RuntimeException("This Operation Can not be completed");
        }*/
        System.out.println("Task Completed..." + index);
        return index;
    }

    public static void main(String[] args) throws Exception {
        ExecutorService executorService = Executors.newFixedThreadPool(50);
        int max = 10;
        List<Future<Integer>> results = new ArrayList<>();
        for (int i = 0; i < max; i++) {
            int index = i;
            results.add(executorService.submit(() -> task(max, index)));
        }
        System.out.println("Some other instructions performed");
        for (Future<Integer> result : results) {
            System.out.println(result.get());
        }
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

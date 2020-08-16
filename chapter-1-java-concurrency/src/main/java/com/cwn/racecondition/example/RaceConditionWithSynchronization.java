package com.cwn.racecondition.example;

import com.cwn.racecondition.example.model.LongWrapperWithSynchronization;

public class RaceConditionWithSynchronization {

    public static void main(String[] args) throws InterruptedException {
        LongWrapperWithSynchronization longWrapper = new LongWrapperWithSynchronization();
        Runnable threadImplementation =
                () -> {
                    for (int i = 0; i < 1_000; i++)
                        longWrapper.incrementValue();
                };

        Thread threads[] = new Thread[1_000];
        for (int i = 0; i < 1_000; i++) {
            threads[i] = new Thread(threadImplementation);
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.println(longWrapper.getValue());
    }
}

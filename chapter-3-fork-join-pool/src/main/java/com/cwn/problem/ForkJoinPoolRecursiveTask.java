package com.cwn.problem;

import java.util.concurrent.*;

public class ForkJoinPoolRecursiveTask {
    public static void main(String[] args) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(50);
        TestTask mainTask = new TestTask(10);
        ForkJoinTask<Integer> result = pool.submit(mainTask);
        System.out.println("Main Task result is " + result.join());
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

class TestTask extends RecursiveTask<Integer> {

    private int number;

    public TestTask(int number) {
        this.number = number;
    }

    @Override
    protected Integer compute() {
        if (number == 1) {
            System.out.println("Number for Task is " + number + " Thread :" + Thread.currentThread());
            return 1;
        } else {
            System.out.println("Splitting for Task is " + number + " Thread :" + Thread.currentThread());
            int splitNumber = number / 2;
            TestTask task1 = new TestTask(splitNumber);
            TestTask task2 = new TestTask(splitNumber);
            task1.fork();
            task2.fork();
            return task1.join() + task2.join();
        }
    }
}

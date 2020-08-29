package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

public class ForkJoinPoolRecursiveAction {

    public static void main(String[] args) throws InterruptedException {
        ForkJoinPool pool = new ForkJoinPool(50);
        pool.submit(new TestAction(10));
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

class TestAction extends RecursiveAction {

    private int number;

    public TestAction(int number) {
        this.number = number;
    }

    @Override
    protected void compute() {
        if (number == 1) {
            System.out.println("Number for Task is " + number + " Thread :" + Thread.currentThread());
        } else {
            System.out.println("Splitting for Task is " + number + " Thread :" + Thread.currentThread());
            int splitNumber = number/2;
            invokeAll(new TestAction(splitNumber),new TestAction(splitNumber));
        }
    }
}

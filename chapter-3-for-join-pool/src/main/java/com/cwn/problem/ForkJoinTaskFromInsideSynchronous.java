package com.cwn.problem;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class ForkJoinTaskFromInsideSynchronous {

    public static int divideAndConquerProblem(int number) {
        System.out.println("Number "+number+" is Processing by Thread "+Thread.currentThread());
        if (number <= 1) {
            return 1;
        } else {
            int subtaskNumber = number / 2;

/*            int result1 = divideAndConquerProblem(subtaskNumber);
            int result2 = divideAndConquerProblem(subtaskNumber);*/

            ForkJoinTask<Integer> task1 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            ForkJoinTask<Integer> task2 = ForkJoinTask.adapt(() -> divideAndConquerProblem(subtaskNumber));
            int result1 = task1.invoke();
            int result2 = task2.invoke();
            return result1 + result2;
        }

    }

    public static void main(String[] args) throws InterruptedException {
      /*  System.out.println(divideAndConquerProblem(3)); // 2
        System.out.println(divideAndConquerProblem(6)); // 4
        System.out.println(divideAndConquerProblem(20)); //16*/
        ForkJoinPool pool = new ForkJoinPool(50);
        pool.submit(()-> System.out.println(divideAndConquerProblem(6)));
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
    }
}

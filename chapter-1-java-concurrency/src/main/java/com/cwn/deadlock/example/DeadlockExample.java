package com.cwn.deadlock.example;

import com.cwn.deadlock.example.model.DeadlockModel;

public class DeadlockExample {

    public static void main(String[] args) throws InterruptedException {
        DeadlockModel deadlockModel = new DeadlockModel();

        Runnable threadImplementation = () -> {
            deadlockModel.a();
        };

        Runnable threadImplementation1 = () -> {
            deadlockModel.b();
        };

        Thread thread = new Thread(threadImplementation, "T1");
        Thread thread1 = new Thread(threadImplementation1, "T2");
        thread.start();
        thread1.start();
        thread1.join();
        thread.join();
    }
}

package com.cwn.deadlock.example.model;

public class DeadlockModel {

    private Object key1 = new Object();
    private Object key2 = new Object();

    public void a() {
        synchronized (key1) {
            System.out.println(Thread.currentThread().getName() + " in method A");
            b();
        }
    }

    public void b() {
        synchronized (key2) {
            System.out.println(Thread.currentThread().getName() + " in method B");
            c();
        }
    }

    public void c() {
        synchronized (key1) {
            System.out.println(Thread.currentThread().getName() + " in method C");
        }
    }

}

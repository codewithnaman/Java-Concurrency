package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example14AllOfCompletableFuture {

    private static CurrencyConverter currencyConverter = new CurrencyConverter();

    public static CompletableFuture<String> getCurrencyExchangeRate(String from, String to, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(from, to))
                .thenApply(value -> from + " to " + to + " of amount " + amount + " is " + (value * amount));
    }

    public static void main(String[] args) {
        CompletableFuture<String> task1 = getCurrencyExchangeRate("CAD", "INR", 50);
        CompletableFuture<String> task2 = getCurrencyExchangeRate("CAD", "INR", 60);
        CompletableFuture<String> task3 = getCurrencyExchangeRate("CAD", "INR", 70);
        CompletableFuture<String> task4 = getCurrencyExchangeRate("CAD", "INR", 80);
        CompletableFuture<String> task5 = getCurrencyExchangeRate("CAD", "INR", 90);
        CompletableFuture<String> task6 = getCurrencyExchangeRate("CAD", "INR", 100);
        CompletableFuture<Integer> task7 = getCurrencyExchangeRate("JPY", "INR", 100)
                .thenApply(value -> 5);
        CompletableFuture.allOf(task1, task2, task3, task4, task5, task6, task7)
                .thenRun(() -> System.out.println("All Tasks are completed"))
                .exceptionally(Example14AllOfCompletableFuture::reportIt);
        sleep(7000);
    }

    private static Void reportIt(Throwable throwable) {
        System.out.println(throwable.getMessage());
        throw new RuntimeException(throwable.getMessage());
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

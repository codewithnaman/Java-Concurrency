package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example12RunAfterCompletableFuture {

    private static CurrencyConverter currencyConverter = new CurrencyConverter();

    public static CompletableFuture<String> getCurrencyExchangeRate(String from, String to, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(from, to))
                .thenApply(value -> from + " to " + to + " of amount " + amount + " is " + (value * amount));
    }

    public static void main(String[] args) {
     /*   for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("JPY", "INR", 50);
            task1.runAfterEither(task2, () -> System.out.println("One Task Completed"))
                    .exceptionally(Example12RunAfterCompletableFuture::reportIt);
        }*/
        for (int i = 0; i < 10; i++) {
            CompletableFuture<String> task1 = getCurrencyExchangeRate("USD", "INR", 51);
            CompletableFuture<String> task2 = getCurrencyExchangeRate("JPY", "INR", 50);
            task1.runAfterBoth(task2, () -> System.out.println("Both Tasks Completed"))
                    .exceptionally(Example12RunAfterCompletableFuture::reportIt);
        }
        sleep(5000);
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

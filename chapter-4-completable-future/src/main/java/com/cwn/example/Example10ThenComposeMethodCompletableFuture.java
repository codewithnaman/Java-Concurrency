package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example10ThenComposeMethodCompletableFuture {

    private static CurrencyConverter currencyConverter = new CurrencyConverter();

    public static CompletableFuture<String> getCurrencySymbol(int index) {
        String[] symbols = {"USD", "GBP", "JPY"};
        return CompletableFuture.supplyAsync(() -> symbols[index]);
    }

    public static CompletableFuture<Double> getConversionRateInInr(String symbol, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(symbol, "INR"))
                .thenApply(value -> value * amount);
    }

    public static void main(String[] args) {
        getCurrencySymbol(0)
                .thenCompose(symbol -> getConversionRateInInr(symbol,50))
                .thenAccept(System.out::println);
        sleep(2000);
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

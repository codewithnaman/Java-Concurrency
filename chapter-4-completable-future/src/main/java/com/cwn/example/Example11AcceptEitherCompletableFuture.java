package com.cwn.example;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

public class Example11AcceptEitherCompletableFuture {

    private static CurrencyConverter currencyConverter = new CurrencyConverter();

    public static CompletableFuture<String> getCurrencyExchangeRate(String from, String to, int amount) {
        return CompletableFuture.supplyAsync(() -> currencyConverter.convertCurrency(from, to))
                .thenApply(value -> from + " to " + to + " of amount " + amount + " is " + (value * amount));
    }

    public static void main(String[] args) {
      /*  IntStream.rangeClosed(1, 10).forEach(i ->
                getCurrencyExchangeRate("USD", "INR", 50)
                        .acceptEither(getCurrencyExchangeRate("GBP", "INR", 50), System.out::println));*/
        IntStream.rangeClosed(1, 10).forEach(i ->
                getCurrencyExchangeRate("USD", "INR", 50)
                        .applyToEither(getCurrencyExchangeRate("GBP", "INR", 50),
                                value -> value + " Completed")
                        .thenAccept(System.out::println));
        sleep(10000);
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

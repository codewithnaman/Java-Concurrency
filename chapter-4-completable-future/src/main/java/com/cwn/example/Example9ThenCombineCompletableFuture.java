package com.cwn.example;

import java.util.concurrent.CompletableFuture;

public class Example9ThenCombineCompletableFuture {

    private static CurrencyConverter converter = new CurrencyConverter();

    public static void main(String[] args){
        int usdQuantity = 5;
        int gbpQuantity = 10;
        CompletableFuture
                .supplyAsync(() -> converter.convertCurrency("USD", "INR"))
                .thenApply(value -> usdQuantity * value)
                .thenCombine(CompletableFuture
                                .supplyAsync(() -> converter.convertCurrency("GBP", "INR"))
                                .thenApply(value -> gbpQuantity * value),
                        Double::sum)
                .thenApply(value -> "INR of " + usdQuantity + " USD and " + gbpQuantity + " GPB " + gbpQuantity + "is : " + value)
                .thenAccept(System.out::println);

        sleep(5000);
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

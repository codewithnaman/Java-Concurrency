package com.cwn.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CurrencyConverter {


    public double convertCurrency(String from, String to) {
        if (from.equals("JPY")) {
            sleep(new Random().nextInt(2000));
            throw new RuntimeException("JPY currency can't be converted");
        }
        if(from.equals("CAD")){
            sleep(new Random().nextInt(2000));
        }
        from = from.toUpperCase();
        to = to.toUpperCase();
        String url = "https://api.exchangeratesapi.io/latest?symbols=" + from + "," + to;
        double value = -1;
        try {
            URL webUrl = new URL(url);
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(webUrl.openStream()))) {
                String response = reader.lines().findFirst().get();
                List<String> responseSplitByColon = Arrays.asList(response.split(":"))
                        .stream()
                        .map(word -> {
                            word = word.replace("\"", "");
                            word = word.replace("{", "");
                            word = word.replace("}", "");
                            return word;
                        })
                        .flatMap(word -> Arrays.stream(word.split(",")))
                        .collect(Collectors.toList())
                        .subList(1, 5);
                if (!responseSplitByColon.isEmpty()) {
                    if (responseSplitByColon.get(0).equals(to)) {
                        value = Double.valueOf(responseSplitByColon.get(1)) / Double.valueOf(responseSplitByColon.get(3));
                    } else {
                        value = Double.valueOf(responseSplitByColon.get(3)) / Double.valueOf(responseSplitByColon.get(1));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return value;
    }

    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

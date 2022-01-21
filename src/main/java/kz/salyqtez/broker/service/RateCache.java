package kz.salyqtez.broker.service;

import java.util.HashMap;
import java.util.Map;

public class RateCache {
    public static Map<Long, Double> val = new HashMap<>();

    public static void clear(){
        val = new HashMap<>();
    }
}

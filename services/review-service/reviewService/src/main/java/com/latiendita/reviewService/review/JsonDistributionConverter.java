package com.latiendita.reviewService.review;

import java.util.LinkedHashMap;
import java.util.Map;

/** Star distribution helpers for {@link ProductRatingEntity}. */
public final class JsonDistributionConverter {

    private JsonDistributionConverter() {
    }

    public static Map<String, Integer> emptyDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            distribution.put(String.valueOf(star), 0);
        }
        return distribution;
    }
}

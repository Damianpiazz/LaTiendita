package com.latiendita.reviewService.review;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.LinkedHashMap;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Persists the star distribution map as a jsonb column. */
@Converter
public class JsonDistributionConverter implements AttributeConverter<Map<String, Integer>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Integer>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(Map<String, Integer> attribute) {
        if (attribute == null) {
            return "{}";
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not serialize star distribution", ex);
        }
    }

    @Override
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return emptyDistribution();
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not deserialize star distribution", ex);
        }
    }

    public static Map<String, Integer> emptyDistribution() {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (int star = 1; star <= 5; star++) {
            distribution.put(String.valueOf(star), 0);
        }
        return distribution;
    }
}

package com.latiendita.productService.product.dto;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/** Persists product specifications as a JSON array. */
@Converter
public class JsonSpecsConverter implements AttributeConverter<List<ProductSpec>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<ProductSpec>> TYPE = new TypeReference<>() {};

    @Override
    public String convertToDatabaseColumn(List<ProductSpec> attribute) {
        if (attribute == null) {
            return "[]";
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not serialize product specs", ex);
        }
    }

    @Override
    public List<ProductSpec> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return List.of();
        }
        try {
            return MAPPER.readValue(dbData, TYPE);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not deserialize product specs", ex);
        }
    }
}

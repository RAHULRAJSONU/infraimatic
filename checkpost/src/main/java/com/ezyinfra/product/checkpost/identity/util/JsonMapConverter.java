package com.ezyinfra.product.checkpost.identity.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter
public class JsonMapConverter
        implements AttributeConverter<Map<String, Object>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        try {
            return attribute == null ? null : MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON serialization failed", e);
        }
    }

    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        try {
            return dbData == null ? null : MAPPER.readValue(dbData, Map.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("JSON deserialization failed", e);
        }
    }
}
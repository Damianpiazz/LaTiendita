package com.latiendita.customerService.customer.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

/** Canonical segment labels used by the Admin Clientes wireframe. */
public enum CustomerSegment {
    SIN_COMPRAS("sin-compras"),
    RECURRENTE("recurrente"),
    NUEVA("nueva"),
    ACTIVA("activa"),
    INACTIVA("inactiva");

    private final String wireValue;

    CustomerSegment(String wireValue) {
        this.wireValue = wireValue;
    }

    @JsonValue
    public String wireValue() {
        return wireValue;
    }

    /** Accepts both the lowercase wire value and the enum name. */
    public static CustomerSegment fromWireValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim();
        return Arrays.stream(values())
                .filter(segment -> segment.wireValue.equalsIgnoreCase(normalized)
                        || segment.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElse(null);
    }
}

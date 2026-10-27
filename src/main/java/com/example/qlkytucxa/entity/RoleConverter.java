package com.example.qlkytucxa.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Locale;

/**
 * Maps application roles to the values used by existing databases.
 * Accepts both legacy values (ADMIN) and Java enum names (ROLE_ADMIN).
 */
@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    public String convertToDatabaseColumn(Role role) {
        if (role == null) return null;
        return role.name().startsWith("ROLE_") ? role.name().substring("ROLE_".length()) : role.name();
    }

    @Override
    public Role convertToEntityAttribute(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.startsWith("ROLE_")) normalized = "ROLE_" + normalized;
        return Role.valueOf(normalized);
    }
}

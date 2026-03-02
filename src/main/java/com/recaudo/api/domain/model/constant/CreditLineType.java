package com.recaudo.api.domain.model.constant;

import java.text.Normalizer;

public enum CreditLineType {
    FINANCIAMIENTO("LÍNEA DE FINANCIAMIENTO"),
    LIBRE_INVERSION("LÍNEA LIBRE INVERSION");

    private final String displayName;

    CreditLineType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")        // quita tildes
                .replaceAll("[^A-Za-z0-9 ]", " ") // quita caracteres especiales
                .replaceAll("\\s+", " ")          // espacios múltiples
                .trim()
                .toUpperCase();
    }
    /**
     * Convierte un string a CreditLineType
     * Soporta tanto el nombre completo como el enum name
     */
    public static CreditLineType fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El tipo de línea de crédito no puede ser nulo o vacío"
            );
        }

        String normalizedInput = normalize(text);

        // Comparar contra displayName normalizado
        for (CreditLineType type : CreditLineType.values()) {
            if (normalize(type.displayName).equals(normalizedInput)) {
                return type;
            }
        }

        // Comparar contra enum name
        for (CreditLineType type : CreditLineType.values()) {
            if (type.name().equals(normalizedInput.replace(" ", "_"))) {
                return type;
            }
        }

        // Fallback por palabras clave
        if (normalizedInput.contains("FINANCIAMIENTO")) {
            return FINANCIAMIENTO;
        }

        if (normalizedInput.contains("LIBRE") && normalizedInput.contains("INVERSION")) {
            return LIBRE_INVERSION;
        }

        throw new IllegalArgumentException(
                "Tipo de línea de crédito no válido: " + text +
                        ". Valores válidos: '" + FINANCIAMIENTO.displayName +
                        "' o '" + LIBRE_INVERSION.displayName + "'"
        );
    }
}

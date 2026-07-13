package com.pikudo.restaurant.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FechaUtil {
    // Formato estándar: "dd/MM/yyyy HH:mm:ss" (Ej: 27/06/2026 11:45:00)
    private static final DateTimeFormatter FORMATO_ESTANDAR = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    /*
     Convierte un LocalDateTime a una cadena de texto formateada para el frontend o los tickets.
     */
    public static String formatearFecha(LocalDateTime fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATO_ESTANDAR);
    }

    /*
     Convierte una cadena de texto de vuelta a un objeto LocalDateTime.
     */
    public static LocalDateTime parsearFecha(String fechaStr) {
        if (fechaStr == null || fechaStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(fechaStr, FORMATO_ESTANDAR);
    }
}

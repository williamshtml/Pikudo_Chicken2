package com.pikudo.restaurant.dto.sunat;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer los datos devueltos por la SUNAT
@Setter              // Genera los métodos para asignar los datos de la consulta externa
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor completo resuelto en una línea por Lombok
public class SunatRucResponseDTO {

    private String ruc;          // Número de RUC de 11 dígitos consultado
    private String razonSocial;  // Nombre legal o denominación de la empresa
    private String estado;       // Estado del contribuyente (ej: "ACTIVO", "INACTIVO")
    private String condicion;    // Condición domiciliaria (ej: "HABIDO", "NO HABIDO")
    private String direccion;    // Dirección fiscal registrada ante la SUNAT
}
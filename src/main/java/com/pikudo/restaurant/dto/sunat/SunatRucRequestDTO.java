package com.pikudo.restaurant.dto.sunat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera el método de lectura para capturar el RUC enviado
@Setter              // Genera el método de escritura para asignarlo en el Controller
@NoArgsConstructor   // Constructor vacío () estándar exigido por Jackson
@AllArgsConstructor  // Constructor lleno para instanciar rápido en pruebas o WebClient/Feign
public class SunatRucRequestDTO {

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos numéricos")
    private String ruc; // Número de RUC de la empresa que solicita factura
}
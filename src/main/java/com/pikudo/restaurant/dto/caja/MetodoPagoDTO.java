package com.pikudo.restaurant.dto.caja;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {

    private Long id;

    @NotBlank(message = "El nombre del método de pago es obligatorio")
    @Size(max = 50, message = "El nombre no puede exceder los 50 caracteres")
    private String nombre; // Ej: "YAPE"

    @NotBlank(message = "El tipo de método de pago es obligatorio (EFECTIVO, TARJETA, DIGITAL)")
    @Size(max = 20, message = "El tipo no puede exceder los 20 caracteres")
    private String tipo;   // Macro categoría para agrupar en el arqueo

    private Boolean activo;
}
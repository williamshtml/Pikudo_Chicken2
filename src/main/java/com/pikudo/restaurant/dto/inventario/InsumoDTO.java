package com.pikudo.restaurant.dto.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsumoDTO {

    private Long id;

    @NotBlank(message = "El nombre del insumo es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombre;

    @NotNull(message = "El stock actual es obligatorio")
    @PositiveOrZero(message = "El stock actual no puede ser un número negativo")
    private BigDecimal stockActual;

    @NotNull(message = "El stock mínimo es obligatorio")
    @PositiveOrZero(message = "El stock mínimo para alertas no puede ser negativo")
    private BigDecimal stockMinimo;

    @NotBlank(message = "La unidad de medida es obligatoria (ej: KG, UND, LT)")
    @Size(max = 20, message = "La unidad de medida es muy larga")
    private String unidadMedida;

    private Boolean estado;
}
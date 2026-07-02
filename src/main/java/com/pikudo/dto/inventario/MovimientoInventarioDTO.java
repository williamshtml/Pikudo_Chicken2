package com.pikudo.dto.inventario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventarioDTO {

    private Long id;

    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;
    private String insumoNombre;

    @NotNull(message = "La cantidad del movimiento es obligatoria")
    @Positive(message = "La cantidad debe ser un número positivo")
    private BigDecimal cantidad;

    @NotBlank(message = "El tipo de movimiento es obligatorio (INGRESO o EGRESO)")
    private String tipoMovimiento; 

    @NotBlank(message = "El motivo del movimiento es obligatorio")
    private String motivo; // Ej: "Compra a proveedor", "Pollo vencido"

    private String usuarioUsername; // Quién hizo la operación en el sistema
    private LocalDateTime fechaCreacion; // Fecha del movimiento (auditoría)
}
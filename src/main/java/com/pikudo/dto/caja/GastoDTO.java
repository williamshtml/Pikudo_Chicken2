package com.pikudo.dto.caja;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
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
public class GastoDTO {

    private Long id;

    @NotNull(message = "El ID del turno de caja es obligatorio")
    private Long cajaTurnoId;

    @NotNull(message = "El monto del gasto es obligatorio")
    @Positive(message = "El monto del gasto debe ser mayor a cero")
    private BigDecimal monto;

    @NotBlank(message = "La descripción del gasto es obligatoria")
    @Size(max = 255, message = "La descripción es demasiado larga")
    private String descripcion;

    private String usuarioUsername; // Quién registró el egreso
    private LocalDateTime fechaCreacion;
}
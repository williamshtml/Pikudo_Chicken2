package com.pikudo.dto.orders;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class OrderPaymentRequestDTO {

    @NotBlank(message = "El metodo de pago es obligatorio")
    private String metodoPago;

    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a cero")
    private BigDecimal monto;

    @Size(max = 120, message = "La referencia no puede superar los 120 caracteres")
    private String externalReference;

    @Size(max = 300, message = "La observacion no puede superar los 300 caracteres")
    private String notes;
}

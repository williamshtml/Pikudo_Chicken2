package com.pikudo.restaurant.dto.orders;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderPaymentVoidRequestDTO {

    @NotBlank(message = "El motivo es obligatorio")
    @Size(max = 300, message = "El motivo no puede superar los 300 caracteres")
    private String reason;
}

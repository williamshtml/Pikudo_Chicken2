package com.pikudo.dto.orders;

import com.pikudo.entity.orders.OrderOperationalStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderStatusChangeRequestDTO {

    @NotNull(message = "El estado destino es obligatorio")
    private OrderOperationalStatus status;

    @Size(max = 300, message = "El motivo no puede superar los 300 caracteres")
    private String reason;
}

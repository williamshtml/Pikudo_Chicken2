package com.pikudo.restaurant.dto.delivery;

import com.pikudo.restaurant.entity.delivery.DeliveryStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryStatusRequestDTO {
    @NotNull
    private DeliveryStatus status;

    @Size(max = 255)
    private String reason;
}

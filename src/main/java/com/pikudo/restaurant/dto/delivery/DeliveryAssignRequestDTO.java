package com.pikudo.restaurant.dto.delivery;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryAssignRequestDTO {
    @NotNull
    private Long driverId;
}

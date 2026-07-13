package com.pikudo.dto.delivery;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeliveryCreateRequestDTO {
    @Size(max = 255)
    private String destinationReference;
}

package com.pikudo.dto.orders;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TableSessionOpenRequestDTO {

    @Min(value = 1, message = "La cantidad de personas debe ser al menos 1")
    private Integer guestCount;

    @Size(max = 300, message = "Las notas no pueden superar los 300 caracteres")
    private String notes;
}

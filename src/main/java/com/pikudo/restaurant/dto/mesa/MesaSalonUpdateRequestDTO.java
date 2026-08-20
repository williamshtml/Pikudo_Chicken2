package com.pikudo.restaurant.dto.mesa;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MesaSalonUpdateRequestDTO {
    @NotNull(message = "El salón es obligatorio")
    @Min(value = 1, message = "El salón debe ser un valor positivo")
    private Integer salon;
}
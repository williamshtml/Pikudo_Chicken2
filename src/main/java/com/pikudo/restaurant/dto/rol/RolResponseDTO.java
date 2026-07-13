package com.pikudo.restaurant.dto.rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolResponseDTO {
    private Long id;
    private String nombre; // ADMINISTRADOR, CAJERO, MOZO, MOTORIZADO
}
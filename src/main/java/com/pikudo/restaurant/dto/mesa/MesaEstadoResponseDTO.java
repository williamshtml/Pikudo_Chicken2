package com.pikudo.restaurant.dto.mesa;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MesaEstadoResponseDTO {
    private Long id;
    private Integer numero;
    private Integer capacidad;
    private Integer salon;            // Salón/piso al que pertenece la mesa (1, 2, ...)
    private Boolean activaEnCatalogo;
    private Boolean ocupada; // calculado desde pedidos con estado operativo no terminal
}
package com.pikudo.restaurant.dto.tracking;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RepartidorEstadoDTO {
    private Long id;
    private String username;
    private boolean conectado;
}
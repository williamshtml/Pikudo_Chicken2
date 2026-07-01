package com.pikudo.dto.tracking;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Lo que el celular del repartidor envia al backend.
 * OJO: no incluye repartidorId. El ID se obtiene del usuario autenticado
 * en la sesion WebSocket (ver TrackingController), nunca se confia en un
 * ID que mande el propio cliente, para evitar que alguien falsifique
 * la ubicacion de otro repartidor.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionEntranteDTO {
    private Double lat;
    private Double lng;
}
package com.pikudo.dto.tracking;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "Latitud fuera de rango")
    @DecimalMax(value = "90.0", message = "Latitud fuera de rango")
    private Double lat;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "Longitud fuera de rango")
    @DecimalMax(value = "180.0", message = "Longitud fuera de rango")
    private Double lng;
}
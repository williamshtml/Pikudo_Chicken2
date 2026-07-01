package com.pikudo.dto.tracking;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UbicacionDTO {
    private Long repartidorId;
    private Double lat;
    private Double lng;
    private Long timestamp; // epoch millis
}
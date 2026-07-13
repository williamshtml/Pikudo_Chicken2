package com.pikudo.restaurant.dto.sunat;

import com.pikudo.restaurant.entity.EstadoSunat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResultadoEnvioSunatDTO {
    private EstadoSunat estado;
    private String hash;
    private String mensaje;
    private String urlPdf;
}
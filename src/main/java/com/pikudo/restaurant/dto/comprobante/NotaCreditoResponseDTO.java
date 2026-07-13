package com.pikudo.restaurant.dto.comprobante;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotaCreditoResponseDTO {
    private Long id;
    private Long comprobanteId;
    private String serie;
    private String correlativo;
    private String motivo;
    private BigDecimal montoDevuelto;
    private String usuarioEmisor;
    private LocalDateTime fechaEmision;
}
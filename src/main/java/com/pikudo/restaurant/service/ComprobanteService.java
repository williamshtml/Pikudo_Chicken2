package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.comprobante.AnularComprobanteRequestDTO;
import com.pikudo.restaurant.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.restaurant.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.restaurant.dto.comprobante.NotaCreditoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ComprobanteService {
    ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto);
    ComprobanteResponseDTO buscarPorId(Long id);
    List<ComprobanteResponseDTO> listarPorRangoFechas(LocalDate desde, LocalDate hasta);

    // NUEVO
    NotaCreditoResponseDTO anular(Long comprobanteId, AnularComprobanteRequestDTO dto);
}
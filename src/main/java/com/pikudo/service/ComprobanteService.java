package com.pikudo.service;

import com.pikudo.dto.comprobante.AnularComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.dto.comprobante.NotaCreditoResponseDTO;

import java.time.LocalDate;
import java.util.List;

public interface ComprobanteService {
    ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto);
    ComprobanteResponseDTO buscarPorId(Long id);
    List<ComprobanteResponseDTO> listarPorRangoFechas(LocalDate desde, LocalDate hasta);

    // NUEVO
    NotaCreditoResponseDTO anular(Long comprobanteId, AnularComprobanteRequestDTO dto);
}
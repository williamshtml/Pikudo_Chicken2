package com.pikudo.service;

import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;

public interface ComprobanteService {
    ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto);
    ComprobanteResponseDTO buscarPorId(Long id);
}
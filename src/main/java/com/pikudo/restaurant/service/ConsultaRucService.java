package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.sunat.SunatRucRequestDTO;
import com.pikudo.restaurant.dto.sunat.SunatRucResponseDTO;

public interface ConsultaRucService {
    SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto);
}

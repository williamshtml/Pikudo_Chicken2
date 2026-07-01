package com.pikudo.service;

import com.pikudo.dto.sunat.SunatRucRequestDTO;
import com.pikudo.dto.sunat.SunatRucResponseDTO;

public interface ConsultaRucService {
    SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto);
}

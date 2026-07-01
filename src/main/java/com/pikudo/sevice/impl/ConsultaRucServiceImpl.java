package com.pikudo.sevice.impl;

import com.pikudo.service.ConsultaRucService;
import com.pikudo.dto.sunat.SunatRucRequestDTO;
import com.pikudo.dto.sunat.SunatRucResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ConsultaRucServiceImpl implements ConsultaRucService {

    private final RestTemplate restTemplate; // Inyectado por Spring
    private final String BASE_URL = "https://api.apis.net.pe/v2/sunat/ruc?numero=";

    @Override
    public SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto) {
        try {
            String urlCompleta = BASE_URL + dto.getRuc();
            SunatRucResponseDTO respuesta = restTemplate.getForObject(urlCompleta, SunatRucResponseDTO.class);

            if (respuesta == null) {
                throw new RuntimeException("No se obtuvo respuesta de la SUNAT para el RUC: " + dto.getRuc());
            }
            return respuesta;
        } catch (Exception e) {
            throw new RuntimeException("Error al consultar el RUC en SUNAT: " + e.getMessage());
        }
    }
}
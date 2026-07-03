package com.pikudo.service.impl;

import com.pikudo.service.ConsultaRucService;
import com.pikudo.dto.sunat.SunatRucRequestDTO;
import com.pikudo.dto.sunat.SunatRucResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ConsultaRucServiceImpl implements ConsultaRucService {

    private final RestTemplate restTemplate;

    // Inyectamos la URL desde el archivo application.properties
    @Value("${sunat.api.url}")
    private String baseUrl;

    @Override
    public SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto) {
        try {
            // Construimos la URL usando el valor inyectado
            String urlCompleta = baseUrl + dto.getRuc();
            
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
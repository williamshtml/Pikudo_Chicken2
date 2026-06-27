package com.pikudo.service;

import com.pikudo.dto.sunat.SunatRucRequestDTO;
import com.pikudo.dto.sunat.SunatRucResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ConsultaRucService {

    // Cliente HTTP nativo de Spring Web, ideal para el flujo tradicional de tu API
    private final RestTemplate restTemplate = new RestTemplate(); 
    
    // URL base de la API gratuita de apis.net.pe para el padrón de SUNAT
    private final String BASE_URL = "https://api.apis.net.pe/v2/sunat/ruc?numero=";

    public SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto) {
        try {
            // Concatenamos el RUC de 11 dígitos validado que viene desde el controlador
            String urlCompleta = BASE_URL + dto.getRuc();

            // Hace la petición GET en vivo y mapea el JSON de respuesta directamente a tu DTO
            SunatRucResponseDTO respuesta = restTemplate.getForObject(urlCompleta, SunatRucResponseDTO.class);

            if (respuesta == null) {
                throw new RuntimeException("No se obtuvo respuesta de la SUNAT para el RUC: " + dto.getRuc());
            }

            return respuesta;

        } catch (Exception e) {
            // Captura problemas de red, RUCs inválidos o caídas temporales de la API externa
            throw new RuntimeException("Error al consultar el RUC en SUNAT: " + e.getMessage());
        }
    }
}
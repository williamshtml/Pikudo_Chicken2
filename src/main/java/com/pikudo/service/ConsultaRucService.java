/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.service;

import com.pikudo.dto.sunat.SunatRucRequestDTO;
import com.pikudo.dto.sunat.SunatRucResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class ConsultaRucService {
    /*
     * Se consume la API pública de SUNAT a través de apis.net.pe (gratuita, sin token).
     * Si el proyecto usara la API oficial de SUNAT necesitaría un token OAuth2.
     * URL base: https://api.apis.net.pe/v2/sunat/ruc?numero={ruc}
     */
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.apis.net.pe")
            .build();

    // ─── CONSULTAR RUC ────────────────────────────────────────────────────────
    public SunatRucResponseDTO consultarRuc(SunatRucRequestDTO dto) {
        try {
            // Llama a la API externa y mapea la respuesta directamente al DTO
            SunatRucResponseDTO respuesta = webClient.get()
                    .uri("/v2/sunat/ruc?numero=" + dto.getRuc())
                    .retrieve()
                    .bodyToMono(SunatRucResponseDTO.class)
                    .block(); // Llamada síncrona (válida para un REST controller estándar)

            if (respuesta == null) {
                throw new RuntimeException("No se obtuvo respuesta de la SUNAT para el RUC: " + dto.getRuc());
            }

            return respuesta;

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar el RUC en SUNAT: " + e.getMessage());
        }
    }
}

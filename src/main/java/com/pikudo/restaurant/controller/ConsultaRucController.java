package com.pikudo.restaurant.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/consulta")

public class ConsultaRucController {

    // Simulación o puente rápido para consultar datos de SUNAT/RENIEC mediante API externa
    @GetMapping("/ruc/{ruc}")
    public ResponseEntity<?> consultarRuc(@PathVariable String ruc) {
        if (ruc.length() != 11) {
            return ResponseEntity.badRequest().body("El RUC debe tener 11 dígitos");
        }
        
        // Aquí usualmente consumirías un servicio externo (como APIS PERÚ).
        // Te dejo un mapa de respuesta estructurado para simular la data en lo que acoplas el token externo:
        Map<String, String> dataSimulada = Map.of(
            "ruc", ruc,
            "razonSocial", "POLLERIA PIKUDO CHICKEN S.A.C.",
            "direccion", "Av. Larco 123, Lima",
            "estado", "ACTIVO",
            "condicion", "HABIDO"
        );
        
        return ResponseEntity.ok(dataSimulada);
    }
}
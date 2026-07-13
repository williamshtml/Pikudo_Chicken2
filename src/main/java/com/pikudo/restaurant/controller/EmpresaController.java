package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.entity.ConfiguracionEmpresa;
import com.pikudo.restaurant.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping
    public ResponseEntity<ConfiguracionEmpresa> obtenerDatosEmpresa() {
        // Buscamos la entidad en la base de datos usando el servicio
        // y la mandamos directamente al frontend sin usar mappers
        return ResponseEntity.ok(empresaService.getDatosEmpresa());
    }

    /* 
     * Más adelante, cuando quieras guardar cambios desde la web,
     * puedes agregar el método de actualizar aquí recibiendo y 
     * devolviendo directamente ConfiguracionEmpresa.
     */
}
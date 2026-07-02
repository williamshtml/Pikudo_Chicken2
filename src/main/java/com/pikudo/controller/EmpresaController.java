package com.pikudo.controller;

import com.pikudo.dto.empresa.EmpresaDTO;
import com.pikudo.mapper.EmpresaMapper;
import com.pikudo.service.EmpresaService;
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
    private final EmpresaMapper empresaMapper;

    @GetMapping
    public ResponseEntity<EmpresaDTO> obtenerDatosEmpresa() {
        // 1. Buscamos la entidad en la base de datos usando el servicio
        var empresa = empresaService.getDatosEmpresa();
        
        // 2. Lo convertimos a DTO con el mapper y lo mandamos al frontend
        return ResponseEntity.ok(empresaMapper.toDTO(empresa));
    }

    /* * Más adelante, cuando quieras guardar cambios desde la web, 
     * puedes descomentar esto y agregar el método de actualizar en tu EmpresaService.
     *
     * @PutMapping
     * public ResponseEntity<EmpresaDTO> actualizarDatosEmpresa(@Valid @RequestBody EmpresaDTO dto) {
     * var empresaActualizada = empresaService.actualizarEmpresa(dto);
     * return ResponseEntity.ok(empresaMapper.toDTO(empresaActualizada));
     * }
     */
}
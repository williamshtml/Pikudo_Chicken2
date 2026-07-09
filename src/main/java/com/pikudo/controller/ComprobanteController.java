package com.pikudo.controller;

import com.pikudo.dto.comprobante.AnularComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.dto.comprobante.NotaCreditoResponseDTO;
import com.pikudo.service.ComprobanteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@RequiredArgsConstructor
public class ComprobanteController {

    private final ComprobanteService comprobanteService;

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping
    public ResponseEntity<ComprobanteResponseDTO> emitir(@Valid @RequestBody ComprobanteRequestDTO dto) {
        ComprobanteResponseDTO nuevo = comprobanteService.emitir(dto);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/{id}")
    public ResponseEntity<ComprobanteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(comprobanteService.buscarPorId(id));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @GetMapping("/historial")
    public ResponseEntity<List<ComprobanteResponseDTO>> listarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(comprobanteService.listarPorRangoFechas(desde, hasta));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/{id}/anular")
    public ResponseEntity<NotaCreditoResponseDTO> anular(
            @PathVariable Long id,
            @Valid @RequestBody AnularComprobanteRequestDTO dto) {
        return ResponseEntity.ok(comprobanteService.anular(id, dto));
    }
}
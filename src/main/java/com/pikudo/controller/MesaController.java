package com.pikudo.controller;

import com.pikudo.entity.Mesa;
import com.pikudo.repository.MesaRepository; // O usa tu MesaService si lo tienes creado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")

public class MesaController {

    @Autowired
    private MesaRepository mesaRepository; // Cambiar por MesaService si manejas interfaz de servicio

    // Listar todas las mesas del restaurante
    @GetMapping
    public ResponseEntity<List<Mesa>> listarTodas() {
        return ResponseEntity.ok(mesaRepository.findAll());
    }

    // Buscar una mesa específica
    @GetMapping("/{id}")
    public ResponseEntity<Mesa> buscarPorId(@PathVariable Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        return ResponseEntity.ok(mesa);
    }

  // Actualizar el estado de la mesa (Ej: true = LIBRE, false = OCUPADA)
    @PutMapping("/{id}/estado")
    public ResponseEntity<Mesa> actualizarEstado(
            @PathVariable Long id, 
            @RequestParam Boolean nuevoEstado) { // <-- CAMBIADO A BOOLEAN
        
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con ID: " + id));
        
        mesa.setEstado(nuevoEstado); // Ahora sí compila porque ambos son Boolean
        return ResponseEntity.ok(mesaRepository.save(mesa));
    }}

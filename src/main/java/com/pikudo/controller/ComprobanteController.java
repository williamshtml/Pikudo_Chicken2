package com.pikudo.controller;

import com.pikudo.entity.Comprobante;
import com.pikudo.repository.ComprobanteRepository; // Cambia a ComprobanteService si usan interfaz de servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comprobantes")
@CrossOrigin(origins = "*")
public class ComprobanteController {

    @Autowired
    private ComprobanteRepository comprobanteRepository;

    // 1. GENERAR BOLETA O FACTURA (Cerrar caja y calcular oficialmente el IGV)
    @PostMapping
    public ResponseEntity<Comprobante> crearComprobante(@RequestBody Comprobante comprobante) {
        Comprobante nuevo = comprobanteRepository.save(comprobante);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 2. BUSCAR UN COMPROBANTE POR ID (Para imprimir el ticket térmico)
    @GetMapping("/{id}")
    public ResponseEntity<Comprobante> buscarPorId(@PathVariable Long id) {
        Comprobante comp = comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con ID: " + id));
        return ResponseEntity.ok(comp);
    }

    // 3. HISTORIAL DE VENTAS
    @GetMapping
    public ResponseEntity<List<Comprobante>> listarTodos() {
        return ResponseEntity.ok(comprobanteRepository.findAll());
    }
}
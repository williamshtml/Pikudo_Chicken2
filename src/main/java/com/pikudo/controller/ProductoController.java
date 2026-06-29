package com.pikudo.controller;

import com.pikudo.entity.Producto;
import com.pikudo.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@CrossOrigin(origins = "*") // Permite la comunicación con Angular sin bloqueos de CORS
public class ProductoController {

    @Autowired
    private ProductoRepository productoRepository;

    // 1. LISTAR TODA LA CARTA (Solo los productos activos para venta en la tablet/celular)
    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoRepository.findByEstadoTrue());
    }

    // 2. FILTRAR PLATOS ACTIVOS POR CATEGORÍA (Ej: /api/productos/categoria/1)
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> listarPorCategoria(@PathVariable Long categoriaId) {
        // Usa tu método real del repositorio que valida ID y Estado activo
        return ResponseEntity.ok(productoRepository.findByCategoriaIdAndEstadoTrue(categoriaId));
    }

    // 3. AGREGAR UN NUEVO PLATO O BEBIDA A LA CARTA
    @PostMapping
    public ResponseEntity<Producto> crear(@RequestBody Producto producto) {
        Producto nuevo = productoRepository.save(producto);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }
}
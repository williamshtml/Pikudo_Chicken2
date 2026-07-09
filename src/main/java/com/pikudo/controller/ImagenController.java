package com.pikudo.controller;

import com.pikudo.service.archivo.ArchivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Endpoint generico para subir imagenes. El parametro {tipo} organiza
 * los archivos por carpeta (ej. "productos", "insumos") sin necesitar
 * un controller distinto por cada modulo.
 *
 * Uso desde el frontend: POST /api/imagenes/subir/productos (form-data: file)
 */
@RestController
@RequestMapping("/api/imagenes")
@RequiredArgsConstructor

public class ImagenController {

    private final ArchivoService archivoService;

    @PostMapping("/subir/{tipo}")
    public ResponseEntity<String> subirImagen(
            @PathVariable String tipo,
            @RequestParam("file") MultipartFile file) {

        String rutaGuardada = archivoService.subir(file, tipo);
        return ResponseEntity.ok(rutaGuardada);
    }
}
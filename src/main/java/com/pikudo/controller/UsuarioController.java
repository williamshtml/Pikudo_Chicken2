package com.pikudo.controller;

import com.pikudo.dto.usuario.UsuarioRequestDTO;
import com.pikudo.dto.usuario.UsuarioResponseDTO;
import com.pikudo.service.UsuarioService; // <-- Conectamos con tu interfaz de servicio
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor // Cambiamos @Autowired suelto por constructor limpio de Lombok
@PreAuthorize("hasRole('ADMINISTRADOR')") // 👑 Toda la clase queda blindada solo para el Admin
public class UsuarioController {

    private final UsuarioService usuarioService; 

    // 1. LISTAR TODO EL PERSONAL
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        return ResponseEntity.ok(usuarioService.listarTodos());
    }

    // 2. BUSCAR UN TRABAJADOR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.buscarPorId(id));
    }

    // 3. REGISTRAR UN NUEVO TRABAJADOR (Ahora sí aplica BCrypt y usa DTOs reales)
    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crear(@RequestBody UsuarioRequestDTO dto) {
        UsuarioResponseDTO nuevo = usuarioService.crear(dto);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 4. ELIMINAR O DAR DE BAJA A UN USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        usuarioService.desactivar(id); // Usa tu desactivación lógica segura (estado = false)
        return ResponseEntity.noContent().build();
    }
}
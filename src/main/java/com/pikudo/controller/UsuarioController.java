package com.pikudo.controller;

import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository; // Cambia a UsuarioService si manejan interfaz de servicio
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Para conectar con Angular sin problemas de CORS
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // 1. LISTAR TODO EL PERSONAL (Administrador ve mozos, cocineros, etc.)
    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // 2. BUSCAR UN TRABAJADOR POR ID
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        return ResponseEntity.ok(usuario);
    }

    // 3. REGISTRAR UN NUEVO TRABAJADOR (Ej: Contratación de un nuevo mozo)
    @PostMapping
    public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
        // Nota: Si manejan contraseñas encriptadas, aquí se pasaría por el PasswordEncoder antes de guardar
        Usuario nuevo = usuarioRepository.save(usuario);
        return new ResponseEntity<>(nuevo, HttpStatus.CREATED);
    }

    // 4. ELIMINAR O DAR DE BAJA A UN USUARIO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        
        usuarioRepository.delete(usuario);
        return ResponseEntity.noContent().build();
    }
}
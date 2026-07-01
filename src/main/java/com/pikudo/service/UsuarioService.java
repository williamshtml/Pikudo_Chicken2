package com.pikudo.service;

import com.pikudo.dto.usuario.UsuarioRequestDTO;
import com.pikudo.dto.usuario.UsuarioResponseDTO;
import com.pikudo.entity.Rol;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.RolRepository;
import com.pikudo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // ─── CREAR ────────────────────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class) // 👈 2. Escritura: Asegura rollback total si algo falla al guardar
    public UsuarioResponseDTO crear(UsuarioRequestDTO dto) {
        if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso");
        }

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + dto.getRolId()));

        Usuario usuario = Usuario.builder()
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .rol(rol)
                .build();

        return toDTO(usuarioRepository.save(usuario));
    }

    // ─── LISTAR ACTIVOS ───────────────────────────────────────────────────────
    // Hereda automáticamente 'readOnly = true'
    public List<UsuarioResponseDTO> listarActivos() {
        return usuarioRepository.findByEstadoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR TODOS ─────────────────────────────────────────────────────────
    // Hereda automáticamente 'readOnly = true'
    public List<UsuarioResponseDTO> listarTodos() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    // Hereda automáticamente 'readOnly = true'
    public UsuarioResponseDTO buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        return toDTO(usuario);
    }

    // ─── ACTUALIZAR ───────────────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class) // 👈 Escritura: Modificación segura de credenciales
    public UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));

        Rol rol = rolRepository.findById(dto.getRolId())
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + dto.getRolId()));

        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);

        return toDTO(usuarioRepository.save(usuario));
    }

    // ─── DESACTIVAR (estado = false) ──────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class) // 👈 Escritura: Persistencia segura del cambio de estado
    public void desactivar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
        usuario.setEstado(false);
        usuarioRepository.save(usuario);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private UsuarioResponseDTO toDTO(Usuario u) {
        return new UsuarioResponseDTO(
                u.getId(),
                u.getUsername(),
                u.getUsername(), // nombreCompleto no está en la Entity; se usa username como fallback
                u.getEstado(),
                u.getRol() != null ? u.getRol().getNombre().name() : null,
                null             // token: solo se llena en el Login (AuthService)
        );
    }
}
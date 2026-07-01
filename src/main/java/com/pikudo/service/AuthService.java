package com.pikudo.service;

import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.dto.auth.RegisterRequestDTO;
import com.pikudo.entity.Rol;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.RolRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    // ─── LOGIN ────────────────────────────────────────────────────────────────
    public AuthResponseDTO login(LoginRequestDTO dto) {
        // 1. Delega la validación a Spring Security (lanza excepción si falla)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        // 2. Si llegamos aquí las credenciales son correctas; buscamos el usuario
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + dto.getUsername()));
        // 3. Generamos el JWT
        String token = jwtService.generateToken(usuario);
        return new AuthResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getUsername(),                          // nombreCompleto no está en la Entity
                usuario.getRol().getNombre().name(),
                token
        );
    }
    // ─── REGISTRO ─────────────────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public AuthResponseDTO register(RegisterRequestDTO dto) {
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
        Usuario guardado = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(guardado);
        return new AuthResponseDTO(
                guardado.getId(),
                guardado.getUsername(),
                guardado.getUsername(),
                guardado.getRol().getNombre().name(),
                token
        );
    }
}
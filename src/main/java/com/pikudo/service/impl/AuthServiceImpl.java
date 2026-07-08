package com.pikudo.service.impl;

import com.pikudo.mapper.AuthMapper; // Importa el mapper
import com.pikudo.service.AuthService;
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
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper; // Inyectado

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + dto.getUsername()));

        String token = jwtService.generateToken(usuario);

        // Ahora usamos el mapper
        return authMapper.toAuthResponse(usuario, token);
    }

@Override
@Transactional
public AuthResponseDTO register(RegisterRequestDTO dto) {
    if (usuarioRepository.findByUsername(dto.getUsername()).isPresent()) {
        throw new RuntimeException("El username '" + dto.getUsername() + "' ya está en uso");
    }

    Rol rol = rolRepository.findById(dto.getRolId())
            .orElseThrow(() -> new RuntimeException("Rol no encontrado con id: " + dto.getRolId()));

    // AQUÍ ESTABA EL ERROR: Faltaban asignar los campos personales
    Usuario usuario = Usuario.builder()
            .username(dto.getUsername())
            .password(passwordEncoder.encode(dto.getPassword()))
            .nombre(dto.getNombre())       // <--- AGREGADO
            .apellido(dto.getApellido())   // <--- AGREGADO
            .dni(dto.getDni())             // <--- AGREGADO
            .telefono(dto.getTelefono())   // <--- AGREGADO
            .rol(rol)
            .build();

    Usuario guardado = usuarioRepository.save(usuario);
    String token = jwtService.generateToken(guardado);

    return authMapper.toAuthResponse(guardado, token);
}
}
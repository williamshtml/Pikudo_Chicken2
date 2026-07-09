package com.pikudo.service.impl;
import com.pikudo.mapper.AuthMapper;
import com.pikudo.service.AuthService;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;

    @Override
    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );
        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getUsername()));
        String token = jwtService.generateToken(usuario);
        return authMapper.toAuthResponse(usuario, token);
    }
}
package com.pikudo.service.impl;

import com.pikudo.dto.auth.AuthMeResponseDTO;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.dto.auth.RefreshTokenRequestDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.mapper.AuthMapper;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.security.JwtService;
import com.pikudo.security.RefreshTokenService;
import com.pikudo.service.AuthService;
import com.pikudo.service.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private static final String OUTCOME_SUCCESS = "SUCCESS";
    private static final String OUTCOME_FAILURE = "FAILURE";

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationManager authenticationManager;
    private final AuthMapper authMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO dto, HttpServletRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );
        } catch (BadCredentialsException ex) {
            auditService.record("AUTH_LOGIN", OUTCOME_FAILURE, null, dto.getUsername(), request, "Credenciales invalidas");
            throw ex;
        }

        Usuario usuario = usuarioRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + dto.getUsername()));

        String accessToken = jwtService.generateToken(usuario);
        RefreshTokenService.IssuedRefreshToken refreshToken = refreshTokenService.issue(usuario, request);
        auditService.record("AUTH_LOGIN", OUTCOME_SUCCESS, usuario, usuario.getUsername(), request, "Login exitoso");

        return authMapper.toAuthResponse(
                usuario,
                accessToken,
                refreshToken.rawToken(),
                jwtService.getExpirationMillis()
        );
    }

    @Override
    @Transactional
    public AuthResponseDTO refresh(RefreshTokenRequestDTO dto, HttpServletRequest request) {
        try {
            RefreshTokenService.TokenRotation rotation = refreshTokenService.rotate(dto.getRefreshToken(), request);
            Usuario usuario = rotation.usuario();
            String accessToken = jwtService.generateToken(usuario);
            auditService.record("AUTH_REFRESH", OUTCOME_SUCCESS, usuario, usuario.getUsername(), request, "Refresh token rotado");
            return authMapper.toAuthResponse(
                    usuario,
                    accessToken,
                    rotation.rawToken(),
                    jwtService.getExpirationMillis()
            );
        } catch (RuntimeException ex) {
            auditService.record("AUTH_REFRESH", OUTCOME_FAILURE, null, null, request, ex.getMessage());
            throw ex;
        }
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequestDTO dto, HttpServletRequest request) {
        try {
            Usuario usuario = refreshTokenService.revoke(dto.getRefreshToken());
            auditService.record("AUTH_LOGOUT", OUTCOME_SUCCESS, usuario, usuario.getUsername(), request, "Refresh token revocado");
        } catch (RuntimeException ex) {
            auditService.record("AUTH_LOGOUT", OUTCOME_FAILURE, null, null, request, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public AuthMeResponseDTO me(Usuario usuario) {
        return authMapper.toMeResponse(usuario);
    }
}

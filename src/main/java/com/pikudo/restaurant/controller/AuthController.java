package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.auth.AuthMeResponseDTO;
import com.pikudo.restaurant.dto.auth.AuthResponseDTO;
import com.pikudo.restaurant.dto.auth.LoginRequestDTO;
import com.pikudo.restaurant.dto.auth.RefreshTokenRequestDTO;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO credentials,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.login(credentials, request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(
            @Valid @RequestBody RefreshTokenRequestDTO refreshToken,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(authService.refresh(refreshToken, request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRequestDTO refreshToken,
            HttpServletRequest request
    ) {
        authService.logout(refreshToken, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponseDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(authService.me(usuario));
    }
}

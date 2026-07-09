package com.pikudo.controller;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Solo login. La creacion de cuentas de personal (register) se movio a
 * UsuarioController, protegida con @PreAuthorize("hasRole('ADMINISTRADOR')").
 *
 * Motivo: un sistema de punto de venta no debe permitir auto-registro publico
 * con eleccion libre de rol - eso permitiria que cualquiera se cree una cuenta
 * de ADMINISTRADOR. La creacion de personal es una accion administrativa,
 * como en cualquier POS profesional (Toast, Square, etc.).
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }
}
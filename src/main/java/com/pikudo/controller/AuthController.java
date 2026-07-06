package com.pikudo.controller;

import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.dto.auth.RegisterRequestDTO; // <-- Agregamos esta importación
import com.pikudo.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO credentials) {
        // Tu AuthServiceImpl ya hace toda la magia: valida la BD, revisa BCrypt y genera el JWT
        return ResponseEntity.ok(authService.login(credentials));
    } // <-- Aquí se cierra correctamente el método login

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequestDTO dto) {
        // Esto llamará a tu servicio real, aplicará BCrypt y guardará en MySQL
        return ResponseEntity.ok(authService.register(dto));
    } 
}
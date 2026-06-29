package com.pikudo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    // Método de Login básico. Si usan JWT, aquí mapearían sus DTOs de credenciales.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        // Simulación rápida de sesión para desarrollo
        if ("admin".equals(username) && "123456".equals(password)) {
            return ResponseEntity.ok(Map.of(
                "token", "jwt-token-pikudo-xyz123",
                "username", username,
                "role", "ROLE_ADMIN"
            ));
        }
        
        return ResponseEntity.status(401).body("Credenciales incorrectas para el sistema Pikudo");
    }
}
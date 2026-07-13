package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.dto.rol.RolResponseDTO;
import com.pikudo.restaurant.service.RolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final RolService rolService;

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @GetMapping
    public ResponseEntity<List<RolResponseDTO>> listarRoles() {
        return ResponseEntity.ok(rolService.listarTodos());
    }
}
package com.pikudo.dto.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <-- Importante añadir esto

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // <-- Añade esto para construirlo fácil en el Service
public class AuthResponseDTO {          

    private Long id;
    private String username;
    private String nombreCompleto; 
    private String rolNombre;
    private String token;
}
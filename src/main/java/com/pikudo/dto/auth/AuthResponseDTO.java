package com.pikudo.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDTO {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String rolNombre;
    private String token;
    private String refreshToken;
    private String tokenType;
    private Long expiresInMillis;
    private List<String> permisos;
}

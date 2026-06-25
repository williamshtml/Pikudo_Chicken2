package com.pikudo.dto.auth;

/**
 * DTO de salida tras un login o registro exitoso.
 * Contiene el token JWT que el cliente debe usar en las siguientes peticiones,
 * además de datos básicos del usuario autenticado para mostrar en el frontend.
 * NUNCA incluye el password, ni siquiera encriptado, por seguridad.
 * Agregado por: [tu nombre] - Módulo de autenticación.
 */

public class AuthResponseDTO {

    private Long id;
    private String username;
    private String nombreCompleto;
    private String rolNombre;
    private String token;

    public AuthResponseDTO() {
    }

    public AuthResponseDTO(Long id, String username, String nombreCompleto, String rolNombre, String token) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.rolNombre = rolNombre;
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombreCompleto() {
        return nombreCompleto;
    }

    public void setNombreCompleto(String nombreCompleto) {
        this.nombreCompleto = nombreCompleto;
    }

    public String getRolNombre() {
        return rolNombre;
    }

    public void setRolNombre(String rolNombre) {
        this.rolNombre = rolNombre;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
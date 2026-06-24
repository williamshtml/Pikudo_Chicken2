/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.usuario;

/**
 * DTO de salida para devolver datos de un Usuario al cliente.
 * Se usa tanto para login, registro como para listados de personal.
 * NUNCA incluye el password, ni siquiera encriptado, por seguridad.
 * Agregado por: [tu nombre] - Módulo de usuarios.
 */

public class UsuarioResponseDTO {
    
    private Long id;
    private String username;
    private String nombreCompleto;
    private Boolean estado;
    private String rolNombre;
    private String token; // se llena solo en el login; en otros casos queda null

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String username, String nombreCompleto, Boolean estado, String rolNombre) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.rolNombre = rolNombre;
    }

    public UsuarioResponseDTO(Long id, String username, String nombreCompleto, Boolean estado, String rolNombre, String token) {
        this.id = id;
        this.username = username;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
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

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
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

package com.pikudo.service;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;

/**
 * El registro de personal se movio a UsuarioService.crear(), protegido con
 * @PreAuthorize("hasRole('ADMINISTRADOR')") en UsuarioController. AuthService
 * solo maneja login: la creacion de cuentas es una accion administrativa,
 * no un flujo publico de autenticacion.
 */
public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO dto);
}
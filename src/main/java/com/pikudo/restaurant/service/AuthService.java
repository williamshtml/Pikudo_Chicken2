package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.auth.AuthMeResponseDTO;
import com.pikudo.restaurant.dto.auth.AuthResponseDTO;
import com.pikudo.restaurant.dto.auth.LoginRequestDTO;
import com.pikudo.restaurant.dto.auth.RefreshTokenRequestDTO;
import com.pikudo.restaurant.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO dto, HttpServletRequest request);
    AuthResponseDTO refresh(RefreshTokenRequestDTO dto, HttpServletRequest request);
    void logout(RefreshTokenRequestDTO dto, HttpServletRequest request);
    AuthMeResponseDTO me(Usuario usuario);
}

package com.pikudo.service;

import com.pikudo.dto.auth.AuthMeResponseDTO;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.dto.auth.LoginRequestDTO;
import com.pikudo.dto.auth.RefreshTokenRequestDTO;
import com.pikudo.entity.Usuario;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO dto, HttpServletRequest request);
    AuthResponseDTO refresh(RefreshTokenRequestDTO dto, HttpServletRequest request);
    void logout(RefreshTokenRequestDTO dto, HttpServletRequest request);
    AuthMeResponseDTO me(Usuario usuario);
}

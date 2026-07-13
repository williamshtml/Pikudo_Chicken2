package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.usuario.UsuarioRequestDTO;
import com.pikudo.restaurant.dto.usuario.UsuarioResponseDTO;
import com.pikudo.restaurant.dto.usuario.UsuarioUpdateRequestDTO;

import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);
    List<UsuarioResponseDTO> listarActivos();
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO buscarPorId(Long id);
    UsuarioResponseDTO actualizar(Long id, UsuarioUpdateRequestDTO dto); // cambia el tipo de DTO
    void desactivar(Long id);
}
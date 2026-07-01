package com.pikudo.service;

import com.pikudo.dto.usuario.UsuarioRequestDTO;
import com.pikudo.dto.usuario.UsuarioResponseDTO;
import java.util.List;

public interface UsuarioService {
    UsuarioResponseDTO crear(UsuarioRequestDTO dto);
    List<UsuarioResponseDTO> listarActivos();
    List<UsuarioResponseDTO> listarTodos();
    UsuarioResponseDTO buscarPorId(Long id);
    UsuarioResponseDTO actualizar(Long id, UsuarioRequestDTO dto);
    void desactivar(Long id);
}

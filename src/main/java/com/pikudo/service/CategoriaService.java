package com.pikudo.service;

import com.pikudo.dto.cateogira.CategoriaRequestDTO;
import com.pikudo.dto.cateogira.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    List<CategoriaResponseDTO> listarTodas();
    CategoriaResponseDTO buscarPorId(Long id);
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void eliminar(Long id);
}
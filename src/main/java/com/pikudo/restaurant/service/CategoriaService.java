package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.categoria.CategoriaRequestDTO;
import com.pikudo.restaurant.dto.categoria.CategoriaResponseDTO;
import java.util.List;

public interface CategoriaService {
    CategoriaResponseDTO crear(CategoriaRequestDTO dto);
    List<CategoriaResponseDTO> listarTodas();
    CategoriaResponseDTO buscarPorId(Long id);
    CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto);
    void eliminar(Long id);
}

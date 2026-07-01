package com.pikudo.service;

import com.pikudo.dto.producto.ProductoRequestDTO;
import com.pikudo.dto.producto.ProductoResponseDTO;
import java.util.List;

public interface ProductoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    List<ProductoResponseDTO> listarActivos();
    List<ProductoResponseDTO> listarTodos();
    List<ProductoResponseDTO> listarPorCategoria(Long categoriaId);
    ProductoResponseDTO buscarPorId(Long id);
    ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto);
    void desactivar(Long id);
    void reactivar(Long id);
}

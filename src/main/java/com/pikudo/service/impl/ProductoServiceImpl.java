package com.pikudo.service.impl;

import com.pikudo.dto.producto.ProductoRequestDTO;
import com.pikudo.dto.producto.ProductoResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.entity.Producto;
import com.pikudo.mapper.ProductoMapper;
import com.pikudo.repository.CategoriaRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoMapper productoMapper; // Inyectamos el Mapper

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + dto.getCategoriaId()));

        Producto producto = productoMapper.toEntity(dto, categoria);
        return productoMapper.toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByEstadoTrue().stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll().stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndEstadoTrue(categoriaId).stream()
                .map(productoMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        return productoMapper.toDTO(producto);
    }

    @Override
    @Transactional
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada: " + dto.getCategoriaId()));

        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        return productoMapper.toDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    @Override
    @Transactional
    public void reactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + id));
        producto.setEstado(true);
        productoRepository.save(producto);
    }
}
/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.service;

import com.pikudo.dto.producto.ProductoRequestDTO;
import com.pikudo.dto.producto.ProductoResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.entity.Producto;
import com.pikudo.repository.CategoriaRepository;
import com.pikudo.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    // ─── CREAR ────────────────────────────────────────────────────────────────
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + dto.getCategoriaId()));

        Producto producto = Producto.builder()
                .nombre(dto.getNombre())
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .categoria(categoria)
                .build();

        return toDTO(productoRepository.save(producto));
    }

    // ─── LISTAR ACTIVOS (carta del mozo) ──────────────────────────────────────
    public List<ProductoResponseDTO> listarActivos() {
        return productoRepository.findByEstadoTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR TODOS (administración) ────────────────────────────────────────
    public List<ProductoResponseDTO> listarTodos() {
        return productoRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR POR CATEGORÍA (filtrar carta) ─────────────────────────────────
    public List<ProductoResponseDTO> listarPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaIdAndEstadoTrue(categoriaId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public ProductoResponseDTO buscarPorId(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        return toDTO(producto);
    }

    // ─── ACTUALIZAR ───────────────────────────────────────────────────────────
    public ProductoResponseDTO actualizar(Long id, ProductoRequestDTO dto) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + dto.getCategoriaId()));

        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setCategoria(categoria);

        return toDTO(productoRepository.save(producto));
    }

    // ─── DESACTIVAR (estado = false, ocultar de la carta) ─────────────────────
    public void desactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setEstado(false);
        productoRepository.save(producto);
    }

    // ─── REACTIVAR (estado = true, volver a mostrar en la carta) ──────────────
    public void reactivar(Long id) {
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
        producto.setEstado(true);
        productoRepository.save(producto);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private ProductoResponseDTO toDTO(Producto p) {
        ProductoResponseDTO response = new ProductoResponseDTO();
        response.setId(p.getId());
        response.setNombre(p.getNombre());
        response.setPrecio(p.getPrecio());
        response.setStock(p.getStock());
        response.setEstado(p.getEstado());
        response.setCategoriaNombre(p.getCategoria() != null ? p.getCategoria().getNombre() : null);
        return response;
    }
}
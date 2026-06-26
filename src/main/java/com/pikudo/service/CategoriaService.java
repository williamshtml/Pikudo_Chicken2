package com.pikudo.service;

import com.pikudo.dto.cateogira.CategoriaRequestDTO;
import com.pikudo.dto.cateogira.CategoriaResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    // ─── CREAR ────────────────────────────────────────────────────────────────
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre())
                .build();
        Categoria guardada = categoriaRepository.save(categoria);
        return toDTO(guardada);
    }

    // ─── LISTAR TODAS ─────────────────────────────────────────────────────────
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        return toDTO(categoria);
    }

    // ─── ACTUALIZAR ───────────────────────────────────────────────────────────
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoria.setNombre(dto.getNombre());
        Categoria actualizada = categoriaRepository.save(categoria);
        return toDTO(actualizada);
    }

    // ─── ELIMINAR LÓGICO (estado = false) ────────────────────────────────────
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoria.setEstado(false);
        categoriaRepository.save(categoria);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private CategoriaResponseDTO toDTO(Categoria c) {
        return new CategoriaResponseDTO(
                c.getId(),
                c.getNombre(),
                null   // 'descripcion' existe en el DTO pero no en la Entity; se deja null
        );
    }
}
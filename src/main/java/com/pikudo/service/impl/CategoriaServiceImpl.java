package com.pikudo.service.impl;

import com.pikudo.service.CategoriaService;
import com.pikudo.mapper.CategoriaMapper; // Importa el mapper
import com.pikudo.dto.categoria.CategoriaRequestDTO;
import com.pikudo.dto.categoria.CategoriaResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper; // Inyectado

    @Override
    @Transactional
    public CategoriaResponseDTO crear(CategoriaRequestDTO dto) {
        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre())
                .estado(true) // Buena práctica: inicializar en true
                .build();
        return categoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoriaResponseDTO> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoriaResponseDTO buscarPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        return categoriaMapper.toDTO(categoria);
    }

    @Override
    @Transactional
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoria.setNombre(dto.getNombre());
        return categoriaMapper.toDTO(categoriaRepository.save(categoria));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + id));
        categoria.setEstado(false);
        categoriaRepository.save(categoria);
    }
}
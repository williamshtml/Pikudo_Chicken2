/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.restaurant.validation;

/**
 * Validador de reglas de negocio para Producto.
 * Verifica condiciones que dependen de la base de datos y no pueden resolverse
 * con anotaciones simples: nombre único, categoría existente.
 * Es invocado desde la capa service antes de crear o actualizar un producto.
 * Agregado por: [tu nombre] - Módulo de validaciones.
 */

import com.pikudo.restaurant.dto.producto.ProductoRequestDTO;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.exception.ResourceNotFoundException;
import com.pikudo.restaurant.repository.CategoriaRepository;
import com.pikudo.restaurant.repository.ProductoRepository;
import org.springframework.stereotype.Component;

@Component
public class ProductoValidator {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoValidator(ProductoRepository productoRepository, CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // Valida los datos antes de crear un nuevo producto
    public void validarParaCrear(ProductoRequestDTO dto) {
        // Usa findAll() en vez de existsByNombre, ya que ese método no existe
        // en el ProductoRepository del proyecto y no debemos modificarlo.
        boolean existeNombre = productoRepository.findAll().stream()
                .anyMatch(p -> p.getNombre().equalsIgnoreCase(dto.getNombre()));

        if (existeNombre) {
            throw new BusinessException("Ya existe un producto con el nombre: " + dto.getNombre());
        }
        validarCategoriaExistente(dto.getCategoriaId());
    }

    // Valida los datos antes de actualizar un producto existente
    public void validarParaActualizar(Long productoId, ProductoRequestDTO dto) {
        productoRepository.findAll().stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(dto.getNombre()) && !p.getId().equals(productoId))
                .findFirst()
                .ifPresent(p -> {
                    throw new BusinessException("Ya existe otro producto con el nombre: " + dto.getNombre());
                });
        validarCategoriaExistente(dto.getCategoriaId());
    }

    // Verifica que la categoría exista en base de datos antes de asociarla al producto
    private void validarCategoriaExistente(Long categoriaId) {
        categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new ResourceNotFoundException("La categoría indicada no existe"));
    }
}


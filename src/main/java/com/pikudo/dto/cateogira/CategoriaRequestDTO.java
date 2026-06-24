/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.cateogira;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO de entrada para crear o actualizar una Categoria de productos.
 * Agregado por: [tu nombre] - Módulo de categorías/carta.
 */
public class CategoriaRequestDTO {
    

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 50)
    private String nombre;

    @Size(max = 150)
    private String descripcion;

    public CategoriaRequestDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
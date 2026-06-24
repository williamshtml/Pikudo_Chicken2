/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.producto;
import java.math.BigDecimal;

/**
 * DTO de salida para devolver datos de un Producto al cliente.
 * Agregado por: [tu nombre] - Módulo de productos/carta.
 */
public class ProductoResponseDTO {

    private Long id;
    private String nombre;
    private BigDecimal precio;
    private Integer stock;
    private Boolean estado;
    private String categoriaNombre;

    public ProductoResponseDTO() {
    }

    public ProductoResponseDTO(Long id, String nombre, BigDecimal precio, Integer stock, Boolean estado, String categoriaNombre) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
        this.estado = estado;
        this.categoriaNombre = categoriaNombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Boolean getEstado() {
        return estado;
    }

    public void setEstado(Boolean estado) {
        this.estado = estado;
    }

    public String getCategoriaNombre() {
        return categoriaNombre;
    }

    public void setCategoriaNombre(String categoriaNombre) {
        this.categoriaNombre = categoriaNombre;
    }
}

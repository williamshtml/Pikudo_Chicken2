/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.mesa;

/**
 * DTO de salida para devolver datos de una Mesa al cliente.
 * Agregado por: [tu nombre] - Módulo de mesas/salón.
 */
public class MesaResponseDTO {
    
    private Long id;
    private Integer numero;
    private Integer capacidad;
    private String estado;

    public MesaResponseDTO() {
    }

    public MesaResponseDTO(Long id, Integer numero, Integer capacidad, String estado) {
        this.id = id;
        this.numero = numero;
        this.capacidad = capacidad;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

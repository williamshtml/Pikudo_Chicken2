/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.sunat;

/**
 * DTO de salida con los datos del contribuyente devueltos por la consulta a SUNAT.
 * Agregado por: [tu nombre] - Módulo de integración SUNAT.
 */

public class SunatRucResponseDTO {


    private String ruc;
    private String razonSocial;
    private String estado; // ACTIVO, INACTIVO, BAJA DE OFICIO
    private String condicion; // HABIDO, NO HABIDO
    private String direccion;

    public SunatRucResponseDTO() {
    }

    public SunatRucResponseDTO(String ruc, String razonSocial, String estado, String condicion, String direccion) {
        this.ruc = ruc;
        this.razonSocial = razonSocial;
        this.estado = estado;
        this.condicion = condicion;
        this.direccion = direccion;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getCondicion() {
        return condicion;
    }

    public void setCondicion(String condicion) {
        this.condicion = condicion;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
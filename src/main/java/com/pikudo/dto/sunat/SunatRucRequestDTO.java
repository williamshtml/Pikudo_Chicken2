/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.dto.sunat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;


/**
 * DTO de entrada para consultar datos de un RUC ante SUNAT,
 * usado al momento de generar una FACTURA y validar la Razón Social del cliente.
 * Agregado por: [tu nombre] - Módulo de integración SUNAT.
 */
public class SunatRucRequestDTO {
    
    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    private String ruc;

    public SunatRucRequestDTO() {
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }
}

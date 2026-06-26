/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.validation;

/**
 * Validador de reglas de negocio para el formato y dígito verificador de un RUC peruano.
 * Verifica que tenga 11 dígitos, un tipo de contribuyente válido (10, 15, 17, 20)
 * y que cumpla el algoritmo oficial de dígito verificador de SUNAT.
 * Es invocado manualmente desde la capa service antes de registrar un comprobante tipo FACTURA.
 * Agregado por: [tu nombre] - Módulo de validaciones.
 */

import com.pikudo.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class RucValidator {

    private static final int[] FACTORES = {5, 4, 3, 2, 7, 6, 5, 4, 3, 2};

    // Lanza una excepción si el RUC no es válido; si todo está bien, no hace nada.
    public void validar(String ruc) {
        if (ruc == null || ruc.isBlank()) {
            throw new BusinessException("El RUC es obligatorio");
        }

        if (!ruc.matches("\\d{11}")) {
            throw new BusinessException("El RUC debe tener exactamente 11 dígitos");
        }

        String prefijo = ruc.substring(0, 2);
        if (!prefijo.equals("10") && !prefijo.equals("15") && !prefijo.equals("17") && !prefijo.equals("20")) {
            throw new BusinessException("El RUC ingresado no corresponde a un tipo de contribuyente válido");
        }

        if (!validarDigitoVerificador(ruc)) {
            throw new BusinessException("El RUC ingresado no es válido (dígito verificador incorrecto)");
        }
    }

    // Algoritmo oficial de SUNAT para validar el dígito verificador (módulo 11)
    private boolean validarDigitoVerificador(String ruc) {
        int suma = 0;
        for (int i = 0; i < 10; i++) {
            int digito = Character.getNumericValue(ruc.charAt(i));
            suma += digito * FACTORES[i];
        }

        int resto = suma % 11;
        int digitoCalculado = 11 - resto;

        if (digitoCalculado == 10) {
            digitoCalculado = 0;
        } else if (digitoCalculado == 11) {
            digitoCalculado = 1;
        }

        int digitoVerificadorReal = Character.getNumericValue(ruc.charAt(10));
        return digitoCalculado == digitoVerificadorReal;
    }
}
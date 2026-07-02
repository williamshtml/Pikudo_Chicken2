package com.pikudo.service.impl;

import com.pikudo.entity.ConfiguracionEmpresa;
import com.pikudo.repository.EmpresaRepository;
import com.pikudo.service.EmpresaService; // Importamos la interfaz
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Override
    public ConfiguracionEmpresa getDatosEmpresa() {
        return empresaRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Configuración de empresa no encontrada. Asegúrese de insertar un registro con ID 1 en la tabla configuracion_empresa."));
    }
}
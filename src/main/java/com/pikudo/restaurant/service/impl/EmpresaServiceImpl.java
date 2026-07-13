package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.entity.ConfiguracionEmpresa;
import com.pikudo.restaurant.repository.EmpresaRepository;
import com.pikudo.restaurant.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpresaServiceImpl implements EmpresaService {

    private final EmpresaRepository empresaRepository;

    @Override
    public ConfiguracionEmpresa getDatosEmpresa() {
        return empresaRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException(
                    "Configuración de empresa no encontrada. Asegúrese de insertar un registro con ID 1 en la tabla configuracion_empresa."
                ));
    }
}
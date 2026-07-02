package com.pikudo.mapper;

import com.pikudo.dto.empresa.EmpresaDTO;
import com.pikudo.entity.ConfiguracionEmpresa;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmpresaMapper {
    
    EmpresaDTO toDTO(ConfiguracionEmpresa entidad);
    
    ConfiguracionEmpresa toEntity(EmpresaDTO dto);
}
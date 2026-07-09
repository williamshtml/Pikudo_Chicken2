package com.pikudo.mapper;

import com.pikudo.dto.rol.RolResponseDTO;
import com.pikudo.entity.Rol;
import org.springframework.stereotype.Component;

@Component
public class RolMapper {
    public RolResponseDTO toDTO(Rol rol) {
        if (rol == null) return null;
        return RolResponseDTO.builder()
                .id(rol.getId())
                .nombre(rol.getNombre().name())
                .build();
    }
}
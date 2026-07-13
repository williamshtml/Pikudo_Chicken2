package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.rol.RolResponseDTO;
import com.pikudo.restaurant.entity.Rol;
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
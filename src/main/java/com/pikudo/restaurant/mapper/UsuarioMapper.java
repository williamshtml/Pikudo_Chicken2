package com.pikudo.restaurant.mapper;

import com.pikudo.restaurant.dto.usuario.UsuarioResponseDTO;
import com.pikudo.restaurant.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toDTO(Usuario u) {
        if (u == null) return null;
        
        return UsuarioResponseDTO.builder()
            .id(u.getId())
            .username(u.getUsername())
            .nombre(u.getNombre())
            .apellido(u.getApellido())
            .dni(u.getDni())
            .telefono(u.getTelefono())
            .estado(u.getEstado())
            // Aquí usamos .name() para convertir el Enum a String
            .rolNombre(u.getRol() != null ? u.getRol().getNombre().name() : "SIN ROL")
            .build();
    }
}
    

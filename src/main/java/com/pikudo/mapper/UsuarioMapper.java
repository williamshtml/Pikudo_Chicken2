package com.pikudo.mapper;

import com.pikudo.dto.usuario.UsuarioRequestDTO;
import com.pikudo.dto.usuario.UsuarioResponseDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.entity.Rol;
import org.springframework.stereotype.Component;

@Component

public class UsuarioMapper {
    /*
     Traduce el RequestDTO (Frontend) a la Entidad (Base de Datos).
     */
    public Usuario toEntity(UsuarioRequestDTO dto, Rol rol) {
        if (dto == null) {
            return null;
        }

        return Usuario.builder()
                .username(dto.getUsername())
                .password(dto.getPassword())
                .rol(rol)
                .build();
    }

    /*
     Traduce la Entidad (Base de Datos) al ResponseDTO (Frontend).
     El token se inicializa en null ya que el Service de autenticación se encargará de setearlo si es un login.
     */
    public UsuarioResponseDTO toResponseDTO(Usuario entity) {
        if (entity == null) {
            return null;
        }

        UsuarioResponseDTO response = new UsuarioResponseDTO();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setEstado(entity.getEstado());
        
        // Evitamos un NullPointerException si por alguna razón el usuario no tiene rol asignado
        if (entity.getRol() != null && entity.getRol().getNombre() != null) {
            response.setRolNombre(entity.getRol().getNombre().name()); // .name() convierte el Enum del Rol a String
        }

        return response;
    }
}
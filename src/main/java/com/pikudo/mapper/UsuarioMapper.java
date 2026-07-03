package com.pikudo.mapper;

import com.pikudo.dto.usuario.UsuarioResponseDTO;
import com.pikudo.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioResponseDTO toDTO(Usuario u) {
        if (u == null) return null;
        
        return new UsuarioResponseDTO(
            u.getId(), 
            u.getUsername(), 
            u.getUsername(), // Ajusta si es diferente el nombre completo
            u.getEstado(), 
            u.getRol() != null ? u.getRol().getNombre().name() : null, 
            null // Password suele ir null por seguridad
        );
    }
}
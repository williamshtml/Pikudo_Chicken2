package com.pikudo.mapper;

import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponseDTO toAuthResponse(Usuario usuario, String token) {
        if (usuario == null) return null;

        return new AuthResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getUsername(), // Nombre completo (puedes ajustar si tienes ese campo en Usuario)
                usuario.getRol().getNombre().name(),
                token
        );
    }
}
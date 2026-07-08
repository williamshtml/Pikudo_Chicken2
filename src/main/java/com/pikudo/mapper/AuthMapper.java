package com.pikudo.mapper;

import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.entity.Usuario;
import org.springframework.stereotype.Component;

@Component
public class AuthMapper {

    public AuthResponseDTO toAuthResponse(Usuario usuario, String token) {
        // 1. Protección contra usuario nulo (evita errores al arrancar)
        if (usuario == null) return null;

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                
                // 2. Protección contra nombres nulos (evita "null null")
                .nombreCompleto((usuario.getNombre() != null ? usuario.getNombre() : "") + " " + 
                                (usuario.getApellido() != null ? usuario.getApellido() : ""))
                
                // 3. Protección contra rol nulo (evita error en el .name())
                .rolNombre(usuario.getRol() != null ? usuario.getRol().getNombre().name() : "SIN ROL")
                
                .token(token)
                .build();
    }
}
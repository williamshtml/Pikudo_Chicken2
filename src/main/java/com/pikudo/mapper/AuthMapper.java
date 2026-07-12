package com.pikudo.mapper;

import com.pikudo.dto.auth.AuthMeResponseDTO;
import com.pikudo.dto.auth.AuthResponseDTO;
import com.pikudo.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuthMapper {

    public AuthResponseDTO toAuthResponse(
            Usuario usuario,
            String token,
            String refreshToken,
            long expiresInMillis
    ) {
        if (usuario == null) return null;

        return AuthResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(nombreCompleto(usuario))
                .rolNombre(usuario.getRol() != null ? usuario.getRol().getNombre().name() : "SIN ROL")
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMillis(expiresInMillis)
                .permisos(toPermissionCodes(usuario))
                .build();
    }

    public AuthResponseDTO toAuthResponse(Usuario usuario, String token) {
        return toAuthResponse(usuario, token, null, 0);
    }

    public AuthMeResponseDTO toMeResponse(Usuario usuario) {
        if (usuario == null) return null;

        return AuthMeResponseDTO.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .nombreCompleto(nombreCompleto(usuario))
                .rolNombre(usuario.getRol() != null ? usuario.getRol().getNombre().name() : "SIN ROL")
                .permisos(toPermissionCodes(usuario))
                .build();
    }

    private String nombreCompleto(Usuario usuario) {
        return (usuario.getNombre() != null ? usuario.getNombre() : "") + " " +
                (usuario.getApellido() != null ? usuario.getApellido() : "");
    }

    private List<String> toPermissionCodes(Usuario usuario) {
        return usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> !authority.startsWith("ROLE_"))
                .sorted()
                .toList();
    }
}

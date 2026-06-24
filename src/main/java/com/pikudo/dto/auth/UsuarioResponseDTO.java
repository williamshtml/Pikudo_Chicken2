package com.pikudo.dto.auth;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    private Long id;
    private String nombres;
    private String apellidos;
    private String username;
    private String rolNombre;// Ej: "MOZO" (aplanando la relación)
    private Boolean estado;
}

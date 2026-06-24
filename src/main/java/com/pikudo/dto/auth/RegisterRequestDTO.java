package com.pikudo.dto.auth;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {
    
    private String nombres;
    private String apellidos;
    private String username;
    private String password;
    private Long rolId;// El ID del rol seleccionado en el combo (ADMINISTRADOR=1, CAJERO=2, etc.)
}
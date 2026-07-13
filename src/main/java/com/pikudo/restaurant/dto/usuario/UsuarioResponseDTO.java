package com.pikudo.restaurant.dto.usuario;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // <-- IMPORTANTE: Importar Lombok Builder

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // <-- IMPORTANTE: Agregar esta anotación
public class UsuarioResponseDTO {

    // BORRASTE el método public static Object builder() manual.
    // ¡Lombok lo creará por ti automáticamente al compilar!

    private Long id;
    private String username;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private Boolean estado;
    private String rolNombre;
    private String token;
}
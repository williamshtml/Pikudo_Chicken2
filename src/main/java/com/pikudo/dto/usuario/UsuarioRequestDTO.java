package com.pikudo.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos de lectura automáticamente
@Setter              // Genera los métodos de escritura automáticamente
@NoArgsConstructor   // Constructor vacío () estándar para Jackson
@AllArgsConstructor  // Constructor completo para instanciar rápido en lógica de servicios o pruebas
public class UsuarioRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;       // Credencial única de acceso para el trabajador

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    private String password;       // Contraseña en texto plano (el Service la encriptará antes de guardar)

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
    private String nombreCompleto; // Nombres y apellidos del personal del local

    @NotNull(message = "El rol es obligatorio")
    private Long rolId;            // ID del rol asignado (ej: 1 para Administrador, 2 para Mozo)
}

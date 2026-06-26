package com.pikudo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos de lectura automáticamente
@Setter              // Genera los métodos de escritura automáticamente
@NoArgsConstructor   // Constructor vacío () requerido para que Spring reciba el JSON
@AllArgsConstructor  // Constructor con todos los campos listo para usar en los servicios
public class RegisterRequestDTO {

    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;       // Nickname único con el que el trabajador accederá al sistema

    @NotBlank(message = "El password es obligatorio")
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    private String password;       // Contraseña en texto plano (el Service la encriptará con BCrypt)

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    private String nombreCompleto; // Nombres y apellidos juntos para el formulario de registro

    @NotNull(message = "El rol es obligatorio")
    private Long rolId;            // ID del rol seleccionado en el combo (1=ADMIN, 2=MOZO, etc.)
}
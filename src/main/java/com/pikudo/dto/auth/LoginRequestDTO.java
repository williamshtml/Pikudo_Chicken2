package com.pikudo.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Crea los métodos de lectura automáticamente
@Setter              // Crea los métodos de escritura automáticamente
@NoArgsConstructor   // Constructor vacío () que usa Jackson para procesar el JSON entrante
@AllArgsConstructor  // Constructor lleno para instanciar rápido en pruebas o servicios
public class LoginRequestDTO {

    @NotBlank(message = "El username es obligatorio") // Valida que no llegue vacío ni con puros espacios
    private String username;                           // Nombre de usuario para iniciar sesión

    @NotBlank(message = "El password es obligatorio") // Evita contraseñas vacías desde el formulario
    private String password;                           // Clave en texto plano que luego se validará
}
package com.pikudo.dto.auth;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer los datos desde el frontend
@Setter              // Genera los métodos para escribir datos en el objeto
@NoArgsConstructor   // Constructor vacío () obligatorio para que Spring procese el JSON
@AllArgsConstructor  // Constructor lleno para instanciar todo el objeto de golpe
public class AuthResponseDTO {         

    private Long id;              // ID único del usuario (sirve para mantener la sesión enlazada)
    private String username;      // Nombre de usuario (ej: "juan.mozo") para pintar en el perfil
    private String nombreCompleto;// Nombres y Apellidos ya unidos para mostrar un saludo en la app
    private String rolNombre;     // Texto del rol (ej: "MOZO") para ocultar o mostrar botones en la UI
    private String token;         // El token JWT crucial que el frontend guardará para validar cada petición
}
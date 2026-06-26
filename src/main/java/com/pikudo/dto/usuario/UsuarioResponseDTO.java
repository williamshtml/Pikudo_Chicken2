package com.pikudo.dto.usuario;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter              // Genera los métodos para leer los datos del usuario en el frontend
@Setter              // Genera los métodos para transferir la información desde la Entity
@NoArgsConstructor   // Constructor vacío () requerido para Jackson
@AllArgsConstructor  // Constructor completo que cubre todos los campos (incluyendo token)
public class UsuarioResponseDTO {

    private Long id;              // ID único del usuario en el sistema
    private String username;       // Nombre de usuario / credencial de acceso
    private String nombreCompleto; // Nombres y apellidos del colaborador
    private Boolean estado;       // Estado del usuario (true = Activo, false = Cesado/Inactivo)
    private String rolNombre;     // Nombre limpio del rol asignado (ej: "ADMIN", "MOZO")
    private String token;         // JWT de sesión. Solo se llena en el Login; en CRUDs queda null
}

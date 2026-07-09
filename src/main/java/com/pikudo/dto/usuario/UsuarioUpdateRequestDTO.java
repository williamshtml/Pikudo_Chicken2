package com.pikudo.dto.usuario;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50)
    private String username;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8)
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    private String telefono;

    @NotNull(message = "El rol es obligatorio")
    private Long rolId;

    // Password OPCIONAL: si no se envía (o viene vacío), no se cambia.
    // Sin @NotBlank a propósito — solo se valida el tamaño SI el admin decide cambiarlo.
    @Size(min = 6, message = "El password debe tener al menos 6 caracteres")
    private String password;
}
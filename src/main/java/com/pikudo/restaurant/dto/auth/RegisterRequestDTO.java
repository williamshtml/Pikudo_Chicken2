package com.pikudo.restaurant.dto.auth;
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
public class RegisterRequestDTO {
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 4, max = 50, message = "El username debe tener entre 4 y 50 caracteres")
    private String username;
    @NotBlank(message = "El password es obligatorio")
    @Size(min = 8, message = "El password debe tener al menos 8 caracteres")
    private String password;
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 50)
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 50)
    private String apellido;
    @NotBlank(message = "El DNI es obligatorio")
    @Size(min = 8, max = 8, message = "El DNI debe tener 8 dígitos")
    private String dni;
    @NotBlank(message = "El teléfono es obligatorio")
    @Size(max = 15)
    private String telefono;
    @NotNull(message = "El rol es obligatorio")
    private Long rolId;
}
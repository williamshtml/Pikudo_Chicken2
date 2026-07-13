package com.pikudo.restaurant.entity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(unique = true, nullable = false, length = 60)
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    @JsonIgnore // Defensa adicional: si alguna vez se serializa la entidad por error
                // (en vez del DTO correspondiente), el hash de la contraseña nunca sale en el JSON.
    private String password; // Guarda el HASH de BCrypt, no la contraseña en texto plano.
                              // La validacion real de longitud/politica va en RegisterRequestDTO,
                              // sobre la contraseña original antes de encriptarla.

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false, length = 50)
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    @Column(nullable = false, length = 50)
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "^9[0-9]{8}$", message = "El teléfono debe ser un número móvil peruano válido (9 dígitos, empieza con 9)")
    @Column(nullable = false, length = 15)
    private String telefono;

    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    // --- MÉTODOS DE USERDETAILS ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (this.rol != null && this.rol.getNombre() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.rol.getNombre().name()));
            if (this.rol.getPermissions() != null) {
                this.rol.getPermissions().stream()
                        .filter(permission -> Boolean.TRUE.equals(permission.getEnabled()))
                        .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
                        .forEach(authorities::add);
            }
        }
        return authorities;
    }
    @Override
    public String getPassword() {
        return this.password;
    }
    @Override
    public String getUsername() {
        return this.username;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return this.estado != null && this.estado;
    }
}

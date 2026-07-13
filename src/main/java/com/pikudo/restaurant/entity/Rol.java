package com.pikudo.restaurant.entity;
import com.pikudo.restaurant.entity.security.Permission;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rol {
    @Id                                                 //Establece la clave primaria
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Autoincrementa el ID
    private Long id;
    @Enumerated(EnumType.STRING)                        //El sistema guardara el rol como una cadena en lugar de su indice
    @Column(nullable = false, unique = true, length = 30)
    private TipoRol nombre;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
    
    //Sebastian dice: Hay que agregar un apellido?
    // Este Enum define los únicos roles permitidos en el sistema de la pollería
    public enum TipoRol {
        ADMINISTRADOR,
        CAJERO,
        MOZO,
        MOTORIZADO
    }
}

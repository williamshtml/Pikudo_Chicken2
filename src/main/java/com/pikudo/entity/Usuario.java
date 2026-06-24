package com.pikudo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
    @Id: Define que la variable sera la clave primaria (PK)
    @GeneratedValue: Configura columna como autoincrementaria
    */

    @NotBlank(message = "El nombre de usuario es obligatorio")
    @Column(unique = true, nullable = false, length = 60)
    private String username;
    /*
    @NotBlank: Valida que el username no llegue vacio. De ser asi, se frena la operacion y salta el mensaje indicado
    @Column: No se permite usernames duplicados / No espacios vacios / Limite de caracteres: 60
    */

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;
    /*
    @NotBlank: Lo mismo que con username, se frenara la operacion y saltare el mensaje
    @Column: No espacios vacios / No se indica limite de caracteres, ya que eso lo hara Spring Security
    */
    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;
    /*
    @Builder.Default: Ayuda a @Builder a reconocer por defecto el estado estara en true indicando usuario activo, se usara false para usuarios inactivos
    @Column: No espacios vacios.
    */

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;
    /*
    @ManyToOne: Define la relacion de muchos a uno con la tabla de "roles". fetch ayuda a Spring Security a verificar los permisos
    @JoinColumn: Crea la columna de llave foranea "rol_id" y especifica que es obligatorio que todo usuario tenga un rol.
    */
}
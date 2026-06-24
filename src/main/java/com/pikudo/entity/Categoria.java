package com.pikudo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Categoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
    @Id: Define que la variable sera la clave primaria (PK)
    @GeneratedValue: Configura columna como autoincrementaria
    */

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Column(unique = true, nullable = false, length = 60)
    private String nombre;
    /*
    @NotBlank: Valida que el nombre de la categoria no llegue vacio. De ser asi, se frena la operacion y salta el mensaje indicado
    @Column: No se permite usernames duplicados / No espacios vacios / Limite de caracteres: 60
    */

    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;
    /*
    @Builder.Default: Ayuda a @Builder a reconocer por defecto el estado indicado
    @Column: No espacios vacios.
    */
}

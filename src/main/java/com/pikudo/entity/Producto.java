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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;

@Entity
@Table(name = "productos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Producto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
    @Id: Define que la variable sera la clave primaria (PK)
    @GeneratedValue: Configura columna como autoincrementaria
    */

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Column(nullable = false, length = 120)
    private String nombre; // Ej: "1/4 de Pollo a la Brasa", "Inca Kola 1L"
    /*
    @NotBlank: Valida que el nombre del producto no llegue vacio. De ser asi, se frena la operacion y salta el mensaje indicado
    @Column: No espacios vacios / Limite de caracteres: 120
    */

    @NotNull(message = "El precio es obligatorio")
    @PositiveOrZero(message = "El precio no puede ser menor a cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio; // Se usa BigDecimal ya que garantiza calculos exactos sin tener perdidas
    /*
    @NotNull: Casi lo mismo que @NotBlank solo que este ultimo es más para caracteres que con numeros
    @PositiveOrZero: Como su nombre lo dice, no permite numeros negativos
    @Column: No espacios vacios / 10 caracteres de numeros, de los cuales 2 son decimales
    */

    @NotNull(message = "El stock es obligatorio")
    @PositiveOrZero(message = "El stock no puede ser negativo")
    @Column(nullable = false)
    private Integer stock; // Cantidad disponible (ejemplo: número de porciones de papas o gaseosas)
    /*
    @NotNull es más para los numeros, @Notblank para caracteres
    @PositiveOrZero: No permite numeros negativos
    @Column: No espacios vacios
    */

    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true; // true = Disponible en la carta, false = Agotado/Oculto

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    /*
    @ManyToOne: Define la relacion de muchos a uno con la tabla de "categorias"
    @JoinColumn: Crea la columna de llave foranea "categoria_id" / No espacios vacios
    */
}

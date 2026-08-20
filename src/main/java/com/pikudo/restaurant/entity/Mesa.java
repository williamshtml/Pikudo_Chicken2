package com.pikudo.restaurant.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "mesas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El número de mesa es obligatorio")
    @Positive(message = "El número de mesa debe ser un valor positivo")
    @Column(unique = true, nullable = false)
    private Integer numero;

    @NotNull(message = "La capacidad de la mesa es obligatoria")
    @Positive(message = "La capacidad debe ser mayor a cero")
    @Column(nullable = false)
    private Integer capacidad;

    // Identifica a qué salón/piso pertenece la mesa (1 = Salón 1, 2 = Salón 2, etc.).
    // @ColumnDefault genera "DEFAULT 1" en el ALTER TABLE, para que agregar esta
    // columna NOT NULL sobre una tabla con filas existentes no falle en Postgres/MySQL.
    @Builder.Default
    @ColumnDefault("1")
    @Column(nullable = false)
    private Integer salon = 1;

    // true = mesa activa en catálogo, false = dada de baja (desactivar()).
    // NO significa libre/ocupada — eso se calcula desde pedidos abiertos.
    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;
}
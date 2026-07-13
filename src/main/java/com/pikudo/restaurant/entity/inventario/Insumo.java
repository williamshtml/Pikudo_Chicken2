package com.pikudo.restaurant.entity.inventario;

import com.pikudo.restaurant.entity.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "insumos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Insumo extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 100, unique = true)
    private String nombre;

    @NotNull
    @Column(nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal stockActual = BigDecimal.ZERO;

    @NotNull
    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 3)
    @Builder.Default
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @NotNull
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida; // Ej: "KG", "UND", "LT"

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean estado = true;
}
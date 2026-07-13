package com.pikudo.restaurant.entity.logistica;

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
@Table(name = "zonas_reparto")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZonaReparto extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 100, unique = true)
    private String nombre; // Ej: "Zona Urbana 1", "Sector Norte", "Alrededores"

    @NotNull
    @Column(name = "costo_envio", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal costoEnvio = BigDecimal.ZERO;

    @Column(length = 500)
    private String delimitacion; // Notas o calles límites (ej: "Desde Av. Principal hasta el Óvalo")

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
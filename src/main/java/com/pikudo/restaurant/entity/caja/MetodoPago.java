package com.pikudo.restaurant.entity.caja;

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

@Entity
@Table(name = "metodos_pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPago extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false, length = 50, unique = true)
    private String nombre; // Ej: "EFECTIVO", "TARJETA_VISA", "YAPE", "PLIN"

    @NotNull
    @Column(nullable = false, length = 20)
    private String tipo; // Categoría macro para el cuadre: "EFECTIVO", "TARJETA", "DIGITAL"

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
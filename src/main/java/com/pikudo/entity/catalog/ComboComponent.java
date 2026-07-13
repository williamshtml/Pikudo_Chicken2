package com.pikudo.entity.catalog;

import com.pikudo.entity.Producto;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "combo_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComboComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "combo_producto_id", nullable = false)
    private Producto comboProduct;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "componente_variante_id", nullable = false)
    private ProductoVariante componentVariant;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad = BigDecimal.ONE;

    @Builder.Default
    @Column(nullable = false)
    private Boolean requerido = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean reemplazable = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    @Builder.Default
    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    private String metadataJson = "{}";

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

package com.pikudo.entity.orders;

import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.catalog.Modifier;
import com.pikudo.entity.catalog.ModifierGroup;
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
@Table(name = "order_item_modifiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemModifier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_pedido_id", nullable = false)
    private DetallePedido detallePedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_id")
    private Modifier modifier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modifier_group_id")
    private ModifierGroup modifierGroup;

    @Column(name = "modifier_group_name_snapshot", length = 120)
    private String modifierGroupNameSnapshot;

    @Column(name = "modifier_name_snapshot", nullable = false, length = 120)
    private String modifierNameSnapshot;

    @Builder.Default
    @Column(name = "extra_price_snapshot", nullable = false, precision = 10, scale = 2)
    private BigDecimal extraPriceSnapshot = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 1;

    @Builder.Default
    @Column(name = "total_extra", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalExtra = BigDecimal.ZERO;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

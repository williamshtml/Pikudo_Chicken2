package com.pikudo.entity;

import com.pikudo.entity.orders.OrderOperationalStatus;
import com.pikudo.entity.orders.OrderPaymentStatus;
import com.pikudo.entity.orders.OrderServiceType;
import com.pikudo.entity.orders.OrderSource;
import com.pikudo.entity.orders.TableSession;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter 
@Setter 
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pedido extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ─── CONTROL DE CONCURRENCIA (El blindaje) ─────────────────────────────
    // Spring manejará esto solo. Si 2 chocan, lanza OptimisticLockException
    @Version
    @Column(name = "version")
    private Integer version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesa_id")
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mesero_id")
    private Usuario mesero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cajero_id")
    private Usuario cajero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "table_session_id")
    private TableSession tableSession;

    @Builder.Default
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = "discount_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = "tax_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDING;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_operativo", nullable = false, length = 30)
    private OrderOperationalStatus estadoOperativo = OrderOperationalStatus.UNREAD;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_pago", nullable = false, length = 30)
    private OrderPaymentStatus estadoPago = OrderPaymentStatus.UNPAID;

    @Column(name = "order_code", nullable = false, unique = true, length = 40)
    private String orderCode;

    @Column(name = "tracking_code", length = 80)
    private String trackingCode;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderSource source = OrderSource.DINE_IN;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private OrderServiceType serviceType = OrderServiceType.DINE_IN;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", length = 20)
    private TipoComprobante tipoComprobante;

    @Column(name = "tipo_pedido", length = 20)
    private String tipoPedido;
    
    // ─── CAMPOS ADICIONALES PARA DELIVERY ────────────────────────────────────
    @Column(name = "direccion", length = 255)
    private String direccion;
    
    @Column(name = "url_maps", length = 500)
    private String urlMaps;

    // ─── DATOS DE CONTACTO/NOTA DEL CLIENTE ──────────────────────────────────
    @Column(name = "telefono_cliente", length = 20)
    private String telefonoCliente;

    @Column(name = "observaciones_pedido", length = 250)
    private String observacionesPedido;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();
}

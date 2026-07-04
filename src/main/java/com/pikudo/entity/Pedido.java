package com.pikudo.entity;
import com.pikudo.entity.caja.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
// Importaciones específicas, nada de asteriscos
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
@Builder // Si lo usas en el mapper, lo mantenemos explícito
@NoArgsConstructor // Muy útil para JPA sin ensuciar
@AllArgsConstructor
public class Pedido extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
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
    @Builder.Default
    @NotNull
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;
    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDING;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", length = 20)
    private TipoComprobante tipoComprobante; // <--- AGREGADO PARA EL COMPROBANTE
// ... resto de la clase
    
    @Column(name = "tipo_pedido", length = 20)
    private String tipoPedido;
    
    // ─── CAMPOS ADICIONALES PARA DELIVERY ────────────────────────────────────
    @Column(name = "direccion", length = 255)
    private String direccion;
    @Column(name = "url_maps", length = 500)
    private String urlMaps;

    // ─── datos de contacto/nota del cliente ──
    @Column(name = "telefono_cliente", length = 20)
    private String telefonoCliente;

    @Column(name = "observaciones_pedido", length = 250)
    private String observacionesPedido; // Ej: "VISA, VINAGRETA" o "Paga con 50 soles"

    // ─── NUEVO: metodo de pago real, necesario para el cuadre de caja ──
    // Es la fuente de verdad que usa PedidoRepository.calcularTotalVentasPorMetodoTipo()
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id")
    private MetodoPago metodoPago;

    @Builder.Default // Necesario para que el builder respete el ArrayList vacío
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();
}
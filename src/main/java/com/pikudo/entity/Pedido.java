package com.pikudo.entity;

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

    @Builder.Default // Necesario para que el builder respete el ArrayList vacío
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetallePedido> detalles = new ArrayList<>();
}
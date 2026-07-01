package com.pikudo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La mesa es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    // EL CAMBIO AQUÍ: 
    // Separamos el Mesero (quien atiende) del Cajero (quien cobra)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "mesero_id") // Relación con el mesero
    private Usuario mesero;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "cajero_id") // Relación con el cajero
    private Usuario cajero;

    @Builder.Default
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @NotNull(message = "El total es obligatorio")
    @PositiveOrZero
    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @NotNull(message = "El estado del pedido es obligatorio")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", length = 20)
    private TipoComprobante tipoComprobante;

    @Builder.Default
    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePedido> detalles = new ArrayList<>();
}

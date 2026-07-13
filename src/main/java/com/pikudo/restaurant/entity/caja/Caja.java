package com.pikudo.restaurant.entity.caja;

import com.pikudo.restaurant.entity.Auditable;
import com.pikudo.restaurant.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "cajas_turnos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Caja extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Numero fisico de la caja/punto de venta (ej: "001"), como se ve en el ticket.
    // Distinto del usuario: el numero de caja es del mueble/POS, el usuario es quien lo opera hoy.
    @NotNull
    @Column(name = "numero_caja", nullable = false, length = 10)
    @Builder.Default
    private String numeroCaja = "001";

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // Cajero responsable del turno
    @NotNull
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;
    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;
    @NotNull
    @Column(name = "monto_inicial", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoInicial = BigDecimal.ZERO;
    @Column(name = "monto_ventas_efectivo", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoVentasEfectivo = BigDecimal.ZERO;
    @Column(name = "monto_ventas_tarjeta", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoVentasTarjeta = BigDecimal.ZERO;
    @Column(name = "monto_ventas_digital", precision = 10, scale = 2) // Para Yape / Plin
    @Builder.Default
    private BigDecimal montoVentasDigital = BigDecimal.ZERO;
    @Column(name = "monto_gastos", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal montoGastos = BigDecimal.ZERO;
    @Column(name = "monto_final_real", precision = 10, scale = 2) // Lo que el cajero declara físicamente
    private BigDecimal montoFinalReal;
    @Column(name = "monto_final_sistema", precision = 10, scale = 2) // Lo que calcula el backend automáticamente
    private BigDecimal montoFinalSistema;
    @Column(length = 255)
    private String observaciones; // Ej: "Faltó 2 soles por falta de sencillo"
    @NotNull
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "ABIERTA"; // ABIERTA, CERRADA
}
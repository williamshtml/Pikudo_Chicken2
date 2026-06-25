package com.pikudo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "comprobantes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Comprobante {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El pedido es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido; // Relación 1 a 1: Cada pedido tiene un único comprobante fiscal

    @NotNull(message = "El tipo de comprobante es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_comprobante", nullable = false, length = 20)
    private TipoComprobante tipoComprobante; // BOLETA, FACTURA o TICKET_INTERNO

    @NotBlank(message = "La serie es obligatoria")
    @Column(nullable = false, length = 10)
    private String serie; // Ej: "B001" para boletas, "F001" para facturas

    @NotBlank(message = "El número correlativo es obligatorio")
    @Column(nullable = false, length = 20)
    private String correlativo; // Ej: "00000124"

    @NotBlank(message = "El método de pago es obligatorio")
    @Column(name = "metodo_pago", nullable = false, length = 30)
    private String metodo_pago; // Ej: "EFECTIVO", "TARJETA", "YAPE/PLIN"

    // --- DESGLOSE DE MONTOS (Finanzas exactas) ---

    @NotNull(message = "El monto neto es obligatorio")
    @PositiveOrZero(message = "El monto neto no puede ser negativo")
    @Column(name = "monto_neto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoNeto; // Operación gravada (subtotal antes de impuestos)

    @NotNull(message = "El IGV es obligatorio")
    @PositiveOrZero(message = "El IGV no puede ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal igv; // El 18% del impuesto en Perú

    @NotNull(message = "El monto total es obligatorio")
    @PositiveOrZero(message = "El monto total no puede ser negativo")
    @Column(name = "monto_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoTotal; // Suma de montoNeto + igv (debe coincidir con el total del Pedido)

    // --- DATOS DE FACTURACIÓN (Opcionales: Aplica si tipoComprobante == FACTURA) ---

    @Pattern(regexp = "^[0-9]{11}$", message = "El RUC debe tener exactamente 11 dígitos numéricos")
    @Column(length = 11)
    private String ruc; // RUC del cliente corporativo
    /*
    @Pattern: Esto asegura por completo que si el cajero digita un RUC, este tenga exactamente 11 números. 
    Si ponen letras o menos de 11 dígitos, el sistema lo frenará antes de guardarlo en la base de datos.
    */

    @Column(name = "razon_social", length = 150)
    private String razonSocial; // Nombre de la empresa del cliente

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now(); // Fecha y hora exacta del pago definitivo
}
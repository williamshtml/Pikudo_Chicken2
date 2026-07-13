package com.pikudo.restaurant.entity.caja;

import com.pikudo.restaurant.entity.Auditable;
import com.pikudo.restaurant.entity.Auditable;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.Comprobante;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Representa UN pago dentro de un comprobante. Un comprobante puede tener
 * varias transacciones (ej: mitad efectivo, mitad Yape) - la suma de todas
 * debe coincidir con el monto total del comprobante.
 *
 * Reemplaza el campo Comprobante.metodo_pago (String unico), que no permitia
 * representar pagos divididos.
 */
@Entity
@Table(name = "transacciones_pago")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransaccionPago extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comprobante_id", nullable = false)
    private Comprobante comprobante;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metodo_pago_id", nullable = false)
    private MetodoPago metodoPago;

    @NotNull
    @Positive(message = "El monto de la transaccion debe ser mayor a cero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
}
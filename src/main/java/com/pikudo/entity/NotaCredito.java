package com.pikudo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notas_credito", indexes = {
    @Index(name = "idx_nc_serie_correlativo", columnList = "serie, correlativo", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotaCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El comprobante es obligatorio")
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "comprobante_id", nullable = false, unique = true)
    private Comprobante comprobante; // El comprobante que esta nota anula

    @NotBlank
    @Column(nullable = false, length = 10)
    @Builder.Default
    private String serie = "NC01";

    @NotBlank
    @Column(nullable = false, length = 20)
    private String correlativo;

    @NotBlank(message = "El motivo de anulación es obligatorio")
    @Column(nullable = false, length = 255)
    private String motivo;

    @NotNull
    @Column(name = "monto_devuelto", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoDevuelto; // Coincide con el total del comprobante anulado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_emisor_id")
    private Usuario usuarioEmisor; // Quién autorizó/ejecutó la anulación

    @Builder.Default
    @Column(name = "fecha_emision", nullable = false)
    private LocalDateTime fechaEmision = LocalDateTime.now();
}
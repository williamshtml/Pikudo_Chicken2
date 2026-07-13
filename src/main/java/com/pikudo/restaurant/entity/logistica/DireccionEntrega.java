package com.pikudo.restaurant.entity.logistica;

import com.pikudo.restaurant.entity.Auditable;
import com.pikudo.restaurant.entity.Usuario; // O tu entidad Cliente/Persona si la tienes separada
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

@Entity
@Table(name = "direcciones_entrega")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DireccionEntrega extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Vinculado al cliente que realiza el pedido
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente; 

    @NotNull
    @Column(nullable = false, length = 255)
    private String direccion;

    @Column(length = 100)
    private String referencia; // Ej: "Frente al parque, portón azul"

    @Column(length = 50)
    private String etiqueta; // Ej: "Casa", "Trabajo", "Mamá"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_reparto_id")
    private ZonaReparto zonaReparto; // Conecta la dirección con su tarifa correspondiente

    @NotNull
    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}
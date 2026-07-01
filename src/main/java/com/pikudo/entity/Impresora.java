package com.pikudo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "impresoras")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Impresora {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private AreaPreparacion area; // COCINA, BAR, HORNO

    @Column(nullable = false, length = 45)
    private String ip; // ej. "192.168.1.50"

    @Column(nullable = false)
    private Integer puerto; // normalmente 9100

    @Column(nullable = false)
    private boolean activa; // por si una impresora se malogra y quieres desactivarla sin borrar el registro
}
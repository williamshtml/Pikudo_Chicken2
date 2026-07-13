package com.pikudo.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotNull(message = "El área es obligatoria")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private AreaPreparacion area; 

    @NotBlank(message = "La IP es obligatoria")
    @Pattern(regexp = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$", 
             message = "Formato de IP inválido")
    @Column(nullable = false, length = 45)
    private String ip;

    @NotNull(message = "El puerto es obligatorio")
    @Positive(message = "El puerto debe ser positivo")
    @Column(nullable = false)
    private Integer puerto;

    @Builder.Default
    @Column(nullable = false)
    private boolean activa = true;
}
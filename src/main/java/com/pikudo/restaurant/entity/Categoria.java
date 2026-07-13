package com.pikudo.restaurant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la categoria es obligatorio")
    @Column(unique = true, nullable = false, length = 60)
    private String nombre;

    @Column(unique = true, nullable = false, length = 90)
    private String slug;

    @Column(length = 250)
    private String descripcion;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    @Builder.Default
    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico = true;

    @Builder.Default
    @Column(nullable = false)
    private Boolean disponible = true;

    @Builder.Default
    @Column(name = "metadata_json", nullable = false, columnDefinition = "jsonb")
    private String metadataJson = "{}";

    @Enumerated(EnumType.STRING)
    @Column(name = "area_preparacion", length = 20)
    private AreaPreparacion areaPreparacion;

    @Builder.Default
    @Column(nullable = false)
    private Boolean estado = true;

    @PrePersist
    void prePersist() {
        if (slug == null || slug.isBlank()) {
            slug = fallbackSlug(nombre);
        }
    }

    private String fallbackSlug(String value) {
        String base = value == null ? "categoria" : value.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return (base.isBlank() ? "categoria" : base) + "-" + System.nanoTime();
    }
}

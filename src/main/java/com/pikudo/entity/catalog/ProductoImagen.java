package com.pikudo.entity.catalog;

import com.pikudo.entity.Producto;
import com.pikudo.entity.storage.StorageFile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "producto_imagenes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductoImagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variante_id")
    private ProductoVariante variante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_file_id", nullable = false)
    private StorageFile storageFile;

    @Builder.Default
    @Column(nullable = false)
    private Boolean principal = false;

    @Builder.Default
    @Column(nullable = false)
    private Integer orden = 0;

    @Column(name = "alt_text", length = 160)
    private String altText;

    @Builder.Default
    @Column(name = "visible_publico", nullable = false)
    private Boolean visiblePublico = true;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    void prePersist() {
        if (fechaCreacion == null) {
            fechaCreacion = LocalDateTime.now();
        }
    }
}

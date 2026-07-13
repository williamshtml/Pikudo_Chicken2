package com.pikudo.service.catalog.impl;

import com.pikudo.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.entity.Producto;
import com.pikudo.entity.ProductoTipo;
import com.pikudo.entity.catalog.ProductoImagen;
import com.pikudo.entity.catalog.ProductoVariante;
import com.pikudo.entity.storage.StorageFile;
import com.pikudo.repository.CategoriaRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.catalog.ProductoImagenRepository;
import com.pikudo.repository.catalog.ProductoPrecioHistorialRepository;
import com.pikudo.repository.catalog.ProductoVarianteRepository;
import com.pikudo.repository.storage.StorageFileRepository;
import com.pikudo.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceImplTest {

    private CategoriaRepository categoriaRepository;
    private ProductoRepository productoRepository;
    private ProductoVarianteRepository varianteRepository;
    private ProductoPrecioHistorialRepository precioHistorialRepository;
    private ProductoImagenRepository imagenRepository;
    private StorageFileRepository storageFileRepository;
    private CatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        varianteRepository = mock(ProductoVarianteRepository.class);
        precioHistorialRepository = mock(ProductoPrecioHistorialRepository.class);
        imagenRepository = mock(ProductoImagenRepository.class);
        storageFileRepository = mock(StorageFileRepository.class);
        StorageService storageService = mock(StorageService.class);
        service = new CatalogServiceImpl(
                categoriaRepository,
                productoRepository,
                varianteRepository,
                precioHistorialRepository,
                imagenRepository,
                storageFileRepository,
                storageService
        );
    }

    @Test
    void createsCategoryWithGeneratedSlug() {
        when(categoriaRepository.existsBySlug("pollos-a-la-lena")).thenReturn(false);
        when(categoriaRepository.save(any(Categoria.class))).thenAnswer(invocation -> {
            Categoria category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        var response = service.createCategory(new CatalogCategoryRequestDTO(
                "Pollos a la Lena",
                null,
                "Carta principal",
                1,
                true,
                true,
                true,
                null
        ));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.slug()).isEqualTo("pollos-a-la-lena");
    }

    @Test
    void createsProductWithInitialVariantAndCurrentPrice() {
        Categoria category = Categoria.builder()
                .id(5L)
                .nombre("Pollos")
                .slug("pollos")
                .estado(true)
                .visiblePublico(true)
                .disponible(true)
                .orden(0)
                .build();
        ProductoVariante[] savedVariant = new ProductoVariante[1];

        when(categoriaRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productoRepository.existsBySlug("cuarto-de-pollo")).thenReturn(false);
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto product = invocation.getArgument(0);
            product.setId(10L);
            return product;
        });
        when(varianteRepository.save(any(ProductoVariante.class))).thenAnswer(invocation -> {
            ProductoVariante variant = invocation.getArgument(0);
            variant.setId(20L);
            savedVariant[0] = variant;
            return variant;
        });
        when(varianteRepository.findByProductoIdOrderByOrdenAscIdAsc(10L)).thenAnswer(invocation -> List.of(savedVariant[0]));
        when(imagenRepository.findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(10L)).thenReturn(List.of());

        CatalogProductResponseDTO response = service.createProduct(new CatalogProductRequestDTO(
                "Cuarto de Pollo",
                null,
                "Con papas",
                5L,
                ProductoTipo.SIMPLE,
                new BigDecimal("18.90"),
                30,
                1,
                true,
                true,
                true,
                "Base",
                "POLLO-001"
        ));

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.slug()).isEqualTo("cuarto-de-pollo");
        assertThat(response.currentPrice()).isEqualByComparingTo("18.90");
        assertThat(response.variants()).hasSize(1);
        assertThat(response.variants().get(0).sku()).isEqualTo("POLLO-001");
    }

    @Test
    void linksExistingStorageFileAsProductImage() {
        Categoria category = Categoria.builder().id(5L).nombre("Pollos").slug("pollos").build();
        Producto product = Producto.builder()
                .id(10L)
                .nombre("Cuarto de Pollo")
                .slug("cuarto-de-pollo")
                .categoria(category)
                .precio(new BigDecimal("18.90"))
                .stock(30)
                .build();
        UUID storageId = UUID.randomUUID();
        StorageFile storageFile = StorageFile.builder()
                .id(storageId)
                .downloadUrl("/api/files/" + storageId + "/content")
                .build();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(product));
        when(storageFileRepository.findById(storageId)).thenReturn(Optional.of(storageFile));
        when(imagenRepository.findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(10L)).thenReturn(List.of());
        when(imagenRepository.save(any(ProductoImagen.class))).thenAnswer(invocation -> {
            ProductoImagen image = invocation.getArgument(0);
            image.setId(99L);
            return image;
        });

        var response = service.addImage(10L, new CatalogProductImageLinkRequestDTO(
                storageId,
                null,
                true,
                0,
                "Pollo servido",
                true
        ));

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.storageFileId()).isEqualTo(storageId);
        assertThat(response.primary()).isTrue();
    }
}

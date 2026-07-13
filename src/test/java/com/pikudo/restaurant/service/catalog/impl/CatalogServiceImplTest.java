package com.pikudo.restaurant.service.catalog.impl;

import com.pikudo.restaurant.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogComboComponentRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierGroupRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductModifierGroupRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.restaurant.entity.Categoria;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.ProductoTipo;
import com.pikudo.restaurant.entity.catalog.ComboComponent;
import com.pikudo.restaurant.entity.catalog.Modifier;
import com.pikudo.restaurant.entity.catalog.ModifierGroup;
import com.pikudo.restaurant.entity.catalog.ProductModifierGroup;
import com.pikudo.restaurant.entity.catalog.ProductoImagen;
import com.pikudo.restaurant.entity.catalog.ProductoVariante;
import com.pikudo.restaurant.entity.storage.StorageFile;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.repository.CategoriaRepository;
import com.pikudo.restaurant.repository.ProductoRepository;
import com.pikudo.restaurant.repository.catalog.ComboComponentRepository;
import com.pikudo.restaurant.repository.catalog.ModifierGroupRepository;
import com.pikudo.restaurant.repository.catalog.ModifierRepository;
import com.pikudo.restaurant.repository.catalog.ProductModifierGroupRepository;
import com.pikudo.restaurant.repository.catalog.ProductoImagenRepository;
import com.pikudo.restaurant.repository.catalog.ProductoPrecioHistorialRepository;
import com.pikudo.restaurant.repository.catalog.ProductoVarianteRepository;
import com.pikudo.restaurant.repository.storage.StorageFileRepository;
import com.pikudo.restaurant.service.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CatalogServiceImplTest {

    private CategoriaRepository categoriaRepository;
    private ProductoRepository productoRepository;
    private ProductoVarianteRepository varianteRepository;
    private ProductoPrecioHistorialRepository precioHistorialRepository;
    private ProductoImagenRepository imagenRepository;
    private ModifierGroupRepository modifierGroupRepository;
    private ModifierRepository modifierRepository;
    private ProductModifierGroupRepository productModifierGroupRepository;
    private ComboComponentRepository comboComponentRepository;
    private StorageFileRepository storageFileRepository;
    private CatalogServiceImpl service;

    @BeforeEach
    void setUp() {
        categoriaRepository = mock(CategoriaRepository.class);
        productoRepository = mock(ProductoRepository.class);
        varianteRepository = mock(ProductoVarianteRepository.class);
        precioHistorialRepository = mock(ProductoPrecioHistorialRepository.class);
        imagenRepository = mock(ProductoImagenRepository.class);
        modifierGroupRepository = mock(ModifierGroupRepository.class);
        modifierRepository = mock(ModifierRepository.class);
        productModifierGroupRepository = mock(ProductModifierGroupRepository.class);
        comboComponentRepository = mock(ComboComponentRepository.class);
        storageFileRepository = mock(StorageFileRepository.class);
        StorageService storageService = mock(StorageService.class);
        service = new CatalogServiceImpl(
                categoriaRepository,
                productoRepository,
                varianteRepository,
                precioHistorialRepository,
                imagenRepository,
                modifierGroupRepository,
                modifierRepository,
                productModifierGroupRepository,
                comboComponentRepository,
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
        when(productModifierGroupRepository.findByProductoIdOrderByOrdenAscIdAsc(10L)).thenReturn(List.of());
        when(comboComponentRepository.findByComboProductIdOrderByOrdenAscIdAsc(10L)).thenReturn(List.of());

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

    @Test
    void rejectsModifierGroupWhenMinSelectionIsGreaterThanMaxSelection() {
        assertThatThrownBy(() -> service.createModifierGroup(new CatalogModifierGroupRequestDTO(
                "Guarnicion",
                null,
                null,
                2,
                1,
                true,
                true,
                true,
                0
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("minSelection");
    }

    @Test
    void createsModifierWithExtraPriceInsideGroup() {
        ModifierGroup group = ModifierGroup.builder()
                .id(7L)
                .nombre("Guarnicion")
                .slug("guarnicion")
                .minSelection(1)
                .maxSelection(1)
                .requerido(true)
                .activo(true)
                .visiblePublico(true)
                .orden(0)
                .build();
        when(modifierGroupRepository.findById(7L)).thenReturn(Optional.of(group));
        when(modifierRepository.existsByGroupIdAndSlug(7L, "papas-fritas")).thenReturn(false);
        when(modifierRepository.save(any(Modifier.class))).thenAnswer(invocation -> {
            Modifier modifier = invocation.getArgument(0);
            modifier.setId(8L);
            return modifier;
        });
        when(modifierRepository.findByGroupIdOrderByOrdenAscIdAsc(7L)).thenAnswer(invocation -> List.of(
                Modifier.builder()
                        .id(8L)
                        .group(group)
                        .nombre("Papas fritas")
                        .slug("papas-fritas")
                        .precioExtra(new BigDecimal("2.50"))
                        .activo(true)
                        .visiblePublico(true)
                        .orden(0)
                        .build()
        ));

        var response = service.addModifier(7L, new CatalogModifierRequestDTO(
                "Papas fritas",
                null,
                new BigDecimal("2.50"),
                true,
                true,
                0
        ));

        assertThat(response.modifiers()).hasSize(1);
        assertThat(response.modifiers().get(0).extraPrice()).isEqualByComparingTo("2.50");
    }

    @Test
    void rejectsDuplicatedModifierGroupAssignment() {
        Producto product = product(10L, ProductoTipo.SIMPLE);
        ModifierGroup group = modifierGroup(20L);

        when(productoRepository.findById(10L)).thenReturn(Optional.of(product));
        when(modifierGroupRepository.findById(20L)).thenReturn(Optional.of(group));
        when(productModifierGroupRepository.existsByProductoIdAndGroupId(10L, 20L)).thenReturn(true);

        assertThatThrownBy(() -> service.assignModifierGroup(10L, new CatalogProductModifierGroupRequestDTO(
                20L,
                null,
                null,
                null,
                0
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("ya esta asociado");
    }

    @Test
    void createsComboComponentForComboProduct() {
        Producto combo = product(10L, ProductoTipo.COMBO);
        Producto componentProduct = product(11L, ProductoTipo.SIMPLE);
        ProductoVariante variant = ProductoVariante.builder()
                .id(30L)
                .producto(componentProduct)
                .nombre("Base")
                .precioActual(new BigDecimal("10.00"))
                .stock(10)
                .build();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(combo));
        when(varianteRepository.findById(30L)).thenReturn(Optional.of(variant));
        when(comboComponentRepository.existsByComboProductIdAndComponentVariantId(10L, 30L)).thenReturn(false);
        when(comboComponentRepository.save(any(ComboComponent.class))).thenAnswer(invocation -> {
            ComboComponent component = invocation.getArgument(0);
            component.setId(40L);
            return component;
        });

        var response = service.addComboComponent(10L, new CatalogComboComponentRequestDTO(
                30L,
                new BigDecimal("1.000"),
                true,
                false,
                0
        ));

        assertThat(response.id()).isEqualTo(40L);
        assertThat(response.componentVariantId()).isEqualTo(30L);
        assertThat(response.componentProductName()).isEqualTo("Producto 11");
    }

    @Test
    void rejectsComboComponentForNonComboProduct() {
        when(productoRepository.findById(10L)).thenReturn(Optional.of(product(10L, ProductoTipo.SIMPLE)));

        assertThatThrownBy(() -> service.addComboComponent(10L, new CatalogComboComponentRequestDTO(
                30L,
                BigDecimal.ONE,
                true,
                false,
                0
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("Solo productos COMBO");
    }

    @Test
    void rejectsComboContainingOwnVariant() {
        Producto combo = product(10L, ProductoTipo.COMBO);
        ProductoVariante ownVariant = ProductoVariante.builder()
                .id(30L)
                .producto(combo)
                .nombre("Base")
                .precioActual(new BigDecimal("20.00"))
                .stock(1)
                .build();

        when(productoRepository.findById(10L)).thenReturn(Optional.of(combo));
        when(varianteRepository.findById(30L)).thenReturn(Optional.of(ownVariant));

        assertThatThrownBy(() -> service.addComboComponent(10L, new CatalogComboComponentRequestDTO(
                30L,
                BigDecimal.ONE,
                true,
                false,
                0
        ))).isInstanceOf(BusinessException.class)
                .hasMessageContaining("si mismo");
    }

    private Producto product(Long id, ProductoTipo type) {
        return Producto.builder()
                .id(id)
                .nombre("Producto " + id)
                .slug("producto-" + id)
                .tipoProducto(type)
                .categoria(Categoria.builder().id(1L).nombre("Categoria").slug("categoria").build())
                .precio(new BigDecimal("10.00"))
                .stock(10)
                .build();
    }

    private ModifierGroup modifierGroup(Long id) {
        return ModifierGroup.builder()
                .id(id)
                .nombre("Guarnicion")
                .slug("guarnicion")
                .minSelection(0)
                .maxSelection(2)
                .requerido(false)
                .activo(true)
                .visiblePublico(true)
                .orden(0)
                .build();
    }
}

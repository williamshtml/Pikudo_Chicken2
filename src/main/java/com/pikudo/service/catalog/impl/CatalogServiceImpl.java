package com.pikudo.service.catalog.impl;

import com.pikudo.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.dto.catalog.CatalogCategoryResponseDTO;
import com.pikudo.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageResponseDTO;
import com.pikudo.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.dto.catalog.CatalogProductVariantRequestDTO;
import com.pikudo.dto.catalog.CatalogProductVariantResponseDTO;
import com.pikudo.entity.Categoria;
import com.pikudo.entity.Producto;
import com.pikudo.entity.ProductoTipo;
import com.pikudo.entity.catalog.ProductoImagen;
import com.pikudo.entity.catalog.ProductoPrecioHistorial;
import com.pikudo.entity.catalog.ProductoVariante;
import com.pikudo.entity.storage.StorageFile;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.CategoriaRepository;
import com.pikudo.repository.ProductoRepository;
import com.pikudo.repository.catalog.ProductoImagenRepository;
import com.pikudo.repository.catalog.ProductoPrecioHistorialRepository;
import com.pikudo.repository.catalog.ProductoVarianteRepository;
import com.pikudo.repository.storage.StorageFileRepository;
import com.pikudo.service.catalog.CatalogService;
import com.pikudo.service.storage.StoragePurpose;
import com.pikudo.service.storage.StorageService;
import com.pikudo.service.storage.StorageUploadRequest;
import com.pikudo.service.storage.StoredFile;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CatalogServiceImpl implements CatalogService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final ProductoVarianteRepository varianteRepository;
    private final ProductoPrecioHistorialRepository precioHistorialRepository;
    private final ProductoImagenRepository imagenRepository;
    private final StorageFileRepository storageFileRepository;
    private final StorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogCategoryResponseDTO> listCategories(String search, Boolean active, Boolean publicVisible, Boolean available, Pageable pageable) {
        return categoriaRepository.findAll(categorySpecification(search, active, publicVisible, available), pageable)
                .map(this::toCategoryResponse);
    }

    @Override
    @Transactional
    public CatalogCategoryResponseDTO createCategory(CatalogCategoryRequestDTO request) {
        String slug = resolveCategorySlug(request.slug(), request.name());
        Categoria category = Categoria.builder()
                .nombre(request.name().trim())
                .slug(slug)
                .descripcion(blankToNull(request.description()))
                .orden(defaultInt(request.sortOrder(), 0))
                .visiblePublico(defaultBool(request.publicVisible(), true))
                .disponible(defaultBool(request.available(), true))
                .estado(defaultBool(request.active(), true))
                .areaPreparacion(request.preparationArea())
                .metadataJson("{}")
                .build();
        return toCategoryResponse(categoriaRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogProductResponseDTO> listProducts(String search, Long categoryId, Boolean active, Boolean publicVisible, Boolean available, Pageable pageable) {
        return productoRepository.findAll(productSpecification(search, categoryId, active, publicVisible, available), pageable)
                .map(this::toProductResponse);
    }

    @Override
    @Transactional
    public CatalogProductResponseDTO createProduct(CatalogProductRequestDTO request) {
        Categoria category = categoriaRepository.findById(request.categoryId())
                .orElseThrow(() -> new BusinessException("Categoria no encontrada: " + request.categoryId()));
        String slug = resolveProductSlug(request.slug(), request.name());
        Producto product = Producto.builder()
                .nombre(request.name().trim())
                .slug(slug)
                .descripcion(blankToNull(request.description()))
                .categoria(category)
                .precio(request.basePrice())
                .stock(request.stock())
                .tipoProducto(request.type() != null ? request.type() : ProductoTipo.SIMPLE)
                .orden(defaultInt(request.sortOrder(), 0))
                .visiblePublico(defaultBool(request.publicVisible(), true))
                .disponible(defaultBool(request.available(), true))
                .estado(defaultBool(request.active(), true))
                .metadataJson("{}")
                .build();
        Producto savedProduct = productoRepository.save(product);
        ProductoVariante variant = createVariantEntity(savedProduct, new CatalogProductVariantRequestDTO(
                StringUtils.hasText(request.initialVariantName()) ? request.initialVariantName() : "Base",
                request.sku(),
                request.basePrice(),
                request.stock(),
                0,
                request.publicVisible(),
                request.available()
        ));
        varianteRepository.save(variant);
        precioHistorialRepository.save(ProductoPrecioHistorial.builder()
                .variante(variant)
                .precio(variant.getPrecioActual())
                .vigenciaDesde(LocalDateTime.now())
                .motivo("Precio inicial")
                .build());
        return toProductResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogProductResponseDTO getProduct(Long id) {
        return toProductResponse(findProduct(id));
    }

    @Override
    @Transactional
    public CatalogProductVariantResponseDTO addVariant(Long productId, CatalogProductVariantRequestDTO request) {
        Producto product = findProduct(productId);
        ProductoVariante variant = varianteRepository.save(createVariantEntity(product, request));
        precioHistorialRepository.save(ProductoPrecioHistorial.builder()
                .variante(variant)
                .precio(variant.getPrecioActual())
                .vigenciaDesde(LocalDateTime.now())
                .motivo("Precio inicial de variante")
                .build());
        return toVariantResponse(variant);
    }

    @Override
    @Transactional
    public CatalogProductImageResponseDTO addImage(Long productId, CatalogProductImageLinkRequestDTO request) {
        Producto product = findProduct(productId);
        ProductoVariante variant = request.variantId() == null ? null : findVariantForProduct(productId, request.variantId());
        StorageFile storageFile = storageFileRepository.findById(request.storageFileId())
                .orElseThrow(() -> new BusinessException("Archivo de storage no encontrado: " + request.storageFileId()));
        ProductoImagen image = saveProductImage(
                product,
                variant,
                storageFile,
                request.primary(),
                request.sortOrder(),
                request.altText(),
                request.publicVisible()
        );
        return toImageResponse(image);
    }

    @Override
    @Transactional
    public CatalogProductImageResponseDTO uploadImage(Long productId, Long variantId, MultipartFile file, Boolean primary, Integer sortOrder, String altText, Boolean publicVisible) {
        Producto product = findProduct(productId);
        ProductoVariante variant = variantId == null ? null : findVariantForProduct(productId, variantId);
        try {
            StoredFile storedFile = storageService.upload(new StorageUploadRequest(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                    file.getSize(),
                    StoragePurpose.PRODUCT_IMAGE,
                    "catalog",
                    String.valueOf(productId),
                    null
            ));
            StorageFile storageFile = storageFileRepository.findById(storedFile.id())
                    .orElseThrow(() -> new BusinessException("Metadata de archivo no encontrada despues del upload"));
            ProductoImagen image = saveProductImage(product, variant, storageFile, primary, sortOrder, altText, publicVisible);
            return toImageResponse(image);
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer la imagen del producto: " + e.getMessage());
        }
    }

    private Producto findProduct(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado: " + id));
    }

    private ProductoVariante findVariantForProduct(Long productId, Long variantId) {
        ProductoVariante variant = varianteRepository.findById(variantId)
                .orElseThrow(() -> new BusinessException("Variante no encontrada: " + variantId));
        if (!variant.getProducto().getId().equals(productId)) {
            throw new BusinessException("La variante no pertenece al producto indicado");
        }
        return variant;
    }

    private ProductoVariante createVariantEntity(Producto product, CatalogProductVariantRequestDTO request) {
        return ProductoVariante.builder()
                .producto(product)
                .nombre(request.name().trim())
                .sku(blankToNull(request.sku()))
                .precioActual(request.price())
                .stock(request.stock())
                .orden(defaultInt(request.sortOrder(), 0))
                .visiblePublico(defaultBool(request.publicVisible(), true))
                .disponible(defaultBool(request.available(), true))
                .metadataJson("{}")
                .build();
    }

    private ProductoImagen saveProductImage(Producto product, ProductoVariante variant, StorageFile storageFile, Boolean primary, Integer sortOrder, String altText, Boolean publicVisible) {
        boolean isPrimary = defaultBool(primary, false);
        if (isPrimary) {
            List<ProductoImagen> currentImages = imagenRepository.findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(product.getId());
            currentImages.forEach(image -> image.setPrincipal(false));
            imagenRepository.saveAll(currentImages);
        }
        ProductoImagen image = ProductoImagen.builder()
                .producto(product)
                .variante(variant)
                .storageFile(storageFile)
                .principal(isPrimary)
                .orden(defaultInt(sortOrder, 0))
                .altText(blankToNull(altText))
                .visiblePublico(defaultBool(publicVisible, true))
                .build();
        return imagenRepository.save(image);
    }

    private Specification<Categoria> categorySpecification(String search, Boolean active, Boolean publicVisible, Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase(Locale.ROOT).trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), like),
                        cb.like(cb.lower(root.get("slug")), like)
                ));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("estado"), active));
            }
            if (publicVisible != null) {
                predicates.add(cb.equal(root.get("visiblePublico"), publicVisible));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("disponible"), available));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<Producto> productSpecification(String search, Long categoryId, Boolean active, Boolean publicVisible, Boolean available) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (StringUtils.hasText(search)) {
                String like = "%" + search.toLowerCase(Locale.ROOT).trim() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("nombre")), like),
                        cb.like(cb.lower(root.get("slug")), like)
                ));
            }
            if (categoryId != null) {
                predicates.add(cb.equal(root.get("categoria").get("id"), categoryId));
            }
            if (active != null) {
                predicates.add(cb.equal(root.get("estado"), active));
            }
            if (publicVisible != null) {
                predicates.add(cb.equal(root.get("visiblePublico"), publicVisible));
            }
            if (available != null) {
                predicates.add(cb.equal(root.get("disponible"), available));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private CatalogCategoryResponseDTO toCategoryResponse(Categoria category) {
        return new CatalogCategoryResponseDTO(
                category.getId(),
                category.getNombre(),
                category.getSlug(),
                category.getDescripcion(),
                category.getOrden(),
                category.getVisiblePublico(),
                category.getDisponible(),
                category.getEstado(),
                category.getAreaPreparacion()
        );
    }

    private CatalogProductResponseDTO toProductResponse(Producto product) {
        List<CatalogProductVariantResponseDTO> variants = varianteRepository.findByProductoIdOrderByOrdenAscIdAsc(product.getId()).stream()
                .map(this::toVariantResponse)
                .toList();
        CatalogProductImageResponseDTO mainImage = imagenRepository.findByProductoIdOrderByPrincipalDescOrdenAscIdAsc(product.getId()).stream()
                .findFirst()
                .map(this::toImageResponse)
                .orElse(null);
        return new CatalogProductResponseDTO(
                product.getId(),
                product.getNombre(),
                product.getSlug(),
                product.getDescripcion(),
                product.getTipoProducto(),
                variants.isEmpty() ? product.getPrecio() : variants.get(0).price(),
                product.getStock(),
                product.getVisiblePublico(),
                product.getDisponible(),
                product.getEstado(),
                product.getOrden(),
                toCategoryResponse(product.getCategoria()),
                variants,
                mainImage
        );
    }

    private CatalogProductVariantResponseDTO toVariantResponse(ProductoVariante variant) {
        return new CatalogProductVariantResponseDTO(
                variant.getId(),
                variant.getNombre(),
                variant.getSku(),
                variant.getPrecioActual(),
                variant.getStock(),
                variant.getVisiblePublico(),
                variant.getDisponible(),
                variant.getOrden()
        );
    }

    private CatalogProductImageResponseDTO toImageResponse(ProductoImagen image) {
        StorageFile storageFile = image.getStorageFile();
        return new CatalogProductImageResponseDTO(
                image.getId(),
                storageFile.getId(),
                image.getVariante() != null ? image.getVariante().getId() : null,
                storageFile.getDownloadUrl(),
                image.getPrincipal(),
                image.getOrden(),
                image.getAltText(),
                image.getVisiblePublico()
        );
    }

    private String resolveCategorySlug(String requestedSlug, String name) {
        String slug = slugify(StringUtils.hasText(requestedSlug) ? requestedSlug : name);
        if (StringUtils.hasText(requestedSlug) && categoriaRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe una categoria con slug: " + slug);
        }
        return uniqueSlug(slug, categoriaRepository::existsBySlug);
    }

    private String resolveProductSlug(String requestedSlug, String name) {
        String slug = slugify(StringUtils.hasText(requestedSlug) ? requestedSlug : name);
        if (StringUtils.hasText(requestedSlug) && productoRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe un producto con slug: " + slug);
        }
        return uniqueSlug(slug, productoRepository::existsBySlug);
    }

    private String uniqueSlug(String baseSlug, SlugExists slugExists) {
        String candidate = baseSlug;
        int suffix = 2;
        while (slugExists.exists(candidate)) {
            candidate = baseSlug + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugify(String value) {
        String slug = value == null ? "" : value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return slug.isBlank() ? "item" : slug;
    }

    private String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private Boolean defaultBool(Boolean value, boolean defaultValue) {
        return value != null ? value : defaultValue;
    }

    private Integer defaultInt(Integer value, int defaultValue) {
        return value != null ? value : defaultValue;
    }

    private interface SlugExists {
        boolean exists(String slug);
    }
}

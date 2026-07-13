package com.pikudo.restaurant.service.catalog.impl;

import com.pikudo.restaurant.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogCategoryResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogComboComponentRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogComboComponentResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierGroupRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierGroupResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogModifierResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductImageResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductModifierGroupRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductModifierGroupResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductVariantRequestDTO;
import com.pikudo.restaurant.dto.catalog.CatalogProductVariantResponseDTO;
import com.pikudo.restaurant.entity.Categoria;
import com.pikudo.restaurant.entity.Producto;
import com.pikudo.restaurant.entity.ProductoTipo;
import com.pikudo.restaurant.entity.catalog.ComboComponent;
import com.pikudo.restaurant.entity.catalog.Modifier;
import com.pikudo.restaurant.entity.catalog.ModifierGroup;
import com.pikudo.restaurant.entity.catalog.ProductoImagen;
import com.pikudo.restaurant.entity.catalog.ProductoPrecioHistorial;
import com.pikudo.restaurant.entity.catalog.ProductoVariante;
import com.pikudo.restaurant.entity.catalog.ProductModifierGroup;
import com.pikudo.restaurant.entity.storage.StorageFile;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.repository.CategoriaRepository;
import com.pikudo.restaurant.repository.ProductoRepository;
import com.pikudo.restaurant.repository.catalog.ProductoImagenRepository;
import com.pikudo.restaurant.repository.catalog.ProductoPrecioHistorialRepository;
import com.pikudo.restaurant.repository.catalog.ProductoVarianteRepository;
import com.pikudo.restaurant.repository.catalog.ComboComponentRepository;
import com.pikudo.restaurant.repository.catalog.ModifierGroupRepository;
import com.pikudo.restaurant.repository.catalog.ModifierRepository;
import com.pikudo.restaurant.repository.catalog.ProductModifierGroupRepository;
import com.pikudo.restaurant.repository.storage.StorageFileRepository;
import com.pikudo.restaurant.service.catalog.CatalogService;
import com.pikudo.restaurant.service.storage.StoragePurpose;
import com.pikudo.restaurant.service.storage.StorageService;
import com.pikudo.restaurant.service.storage.StorageUploadRequest;
import com.pikudo.restaurant.service.storage.StoredFile;
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
import java.math.BigDecimal;
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
    private final ModifierGroupRepository modifierGroupRepository;
    private final ModifierRepository modifierRepository;
    private final ProductModifierGroupRepository productModifierGroupRepository;
    private final ComboComponentRepository comboComponentRepository;
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

    @Override
    @Transactional(readOnly = true)
    public Page<CatalogModifierGroupResponseDTO> listModifierGroups(String search, Boolean active, Boolean publicVisible, Pageable pageable) {
        return modifierGroupRepository.findAll(modifierGroupSpecification(search, active, publicVisible), pageable)
                .map(this::toModifierGroupResponse);
    }

    @Override
    @Transactional
    public CatalogModifierGroupResponseDTO createModifierGroup(CatalogModifierGroupRequestDTO request) {
        validateSelection(request.minSelection(), request.maxSelection());
        String slug = resolveModifierGroupSlug(request.slug(), request.name());
        ModifierGroup group = ModifierGroup.builder()
                .nombre(request.name().trim())
                .slug(slug)
                .descripcion(blankToNull(request.description()))
                .minSelection(defaultInt(request.minSelection(), 0))
                .maxSelection(request.maxSelection())
                .requerido(defaultBool(request.required(), false))
                .activo(defaultBool(request.active(), true))
                .visiblePublico(defaultBool(request.publicVisible(), true))
                .orden(defaultInt(request.sortOrder(), 0))
                .metadataJson("{}")
                .build();
        return toModifierGroupResponse(modifierGroupRepository.save(group));
    }

    @Override
    @Transactional(readOnly = true)
    public CatalogModifierGroupResponseDTO getModifierGroup(Long id) {
        return toModifierGroupResponse(findModifierGroup(id));
    }

    @Override
    @Transactional
    public CatalogModifierGroupResponseDTO addModifier(Long groupId, CatalogModifierRequestDTO request) {
        ModifierGroup group = findModifierGroup(groupId);
        String slug = resolveModifierSlug(groupId, request.slug(), request.name());
        Modifier modifier = Modifier.builder()
                .group(group)
                .nombre(request.name().trim())
                .slug(slug)
                .precioExtra(request.extraPrice() != null ? request.extraPrice() : BigDecimal.ZERO)
                .activo(defaultBool(request.active(), true))
                .visiblePublico(defaultBool(request.publicVisible(), true))
                .orden(defaultInt(request.sortOrder(), 0))
                .metadataJson("{}")
                .build();
        modifierRepository.save(modifier);
        return toModifierGroupResponse(group);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogProductModifierGroupResponseDTO> listProductModifierGroups(Long productId) {
        findProduct(productId);
        return productModifierGroupRepository.findByProductoIdOrderByOrdenAscIdAsc(productId).stream()
                .map(this::toProductModifierGroupResponse)
                .toList();
    }

    @Override
    @Transactional
    public CatalogProductModifierGroupResponseDTO assignModifierGroup(Long productId, CatalogProductModifierGroupRequestDTO request) {
        Producto product = findProduct(productId);
        ModifierGroup group = findModifierGroup(request.modifierGroupId());
        validateSelection(request.minSelectionOverride(), request.maxSelectionOverride());
        if (productModifierGroupRepository.existsByProductoIdAndGroupId(productId, request.modifierGroupId())) {
            throw new BusinessException("El grupo de modificadores ya esta asociado al producto");
        }
        ProductModifierGroup assignment = ProductModifierGroup.builder()
                .producto(product)
                .group(group)
                .requeridoOverride(request.requiredOverride())
                .minSelectionOverride(request.minSelectionOverride())
                .maxSelectionOverride(request.maxSelectionOverride())
                .orden(defaultInt(request.sortOrder(), 0))
                .metadataJson("{}")
                .build();
        return toProductModifierGroupResponse(productModifierGroupRepository.save(assignment));
    }

    @Override
    @Transactional
    public void removeModifierGroup(Long productId, Long groupId) {
        ProductModifierGroup assignment = productModifierGroupRepository.findByProductoIdAndGroupId(productId, groupId)
                .orElseThrow(() -> new BusinessException("El grupo no esta asociado al producto indicado"));
        productModifierGroupRepository.delete(assignment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CatalogComboComponentResponseDTO> listComboComponents(Long productId) {
        Producto product = findProduct(productId);
        validateComboProduct(product);
        return comboComponentRepository.findByComboProductIdOrderByOrdenAscIdAsc(productId).stream()
                .map(this::toComboComponentResponse)
                .toList();
    }

    @Override
    @Transactional
    public CatalogComboComponentResponseDTO addComboComponent(Long productId, CatalogComboComponentRequestDTO request) {
        Producto comboProduct = findProduct(productId);
        validateComboProduct(comboProduct);
        ProductoVariante componentVariant = varianteRepository.findById(request.componentVariantId())
                .orElseThrow(() -> new BusinessException("Variante componente no encontrada: " + request.componentVariantId()));
        if (componentVariant.getProducto().getId().equals(productId)) {
            throw new BusinessException("Un combo no puede contener una variante de si mismo");
        }
        if (comboComponentRepository.existsByComboProductIdAndComponentVariantId(productId, request.componentVariantId())) {
            throw new BusinessException("La variante ya esta asociada como componente del combo");
        }
        ComboComponent component = ComboComponent.builder()
                .comboProduct(comboProduct)
                .componentVariant(componentVariant)
                .cantidad(request.quantity() != null ? request.quantity() : BigDecimal.ONE)
                .requerido(defaultBool(request.required(), true))
                .reemplazable(defaultBool(request.replaceable(), false))
                .orden(defaultInt(request.sortOrder(), 0))
                .metadataJson("{}")
                .build();
        return toComboComponentResponse(comboComponentRepository.save(component));
    }

    @Override
    @Transactional
    public void removeComboComponent(Long productId, Long componentId) {
        ComboComponent component = comboComponentRepository.findByIdAndComboProductId(componentId, productId)
                .orElseThrow(() -> new BusinessException("Componente de combo no encontrado para el producto indicado"));
        comboComponentRepository.delete(component);
    }

    private Producto findProduct(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Producto no encontrado: " + id));
    }

    private ModifierGroup findModifierGroup(Long id) {
        return modifierGroupRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Grupo de modificadores no encontrado: " + id));
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

    private Specification<ModifierGroup> modifierGroupSpecification(String search, Boolean active, Boolean publicVisible) {
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
                predicates.add(cb.equal(root.get("activo"), active));
            }
            if (publicVisible != null) {
                predicates.add(cb.equal(root.get("visiblePublico"), publicVisible));
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
                mainImage,
                productModifierGroupRepository.findByProductoIdOrderByOrdenAscIdAsc(product.getId()).stream()
                        .map(this::toProductModifierGroupResponse)
                        .toList(),
                comboComponentRepository.findByComboProductIdOrderByOrdenAscIdAsc(product.getId()).stream()
                        .map(this::toComboComponentResponse)
                        .toList()
        );
    }

    private CatalogModifierGroupResponseDTO toModifierGroupResponse(ModifierGroup group) {
        return new CatalogModifierGroupResponseDTO(
                group.getId(),
                group.getNombre(),
                group.getSlug(),
                group.getDescripcion(),
                group.getMinSelection(),
                group.getMaxSelection(),
                group.getRequerido(),
                group.getActivo(),
                group.getVisiblePublico(),
                group.getOrden(),
                modifierRepository.findByGroupIdOrderByOrdenAscIdAsc(group.getId()).stream()
                        .map(this::toModifierResponse)
                        .toList()
        );
    }

    private CatalogModifierResponseDTO toModifierResponse(Modifier modifier) {
        return new CatalogModifierResponseDTO(
                modifier.getId(),
                modifier.getNombre(),
                modifier.getSlug(),
                modifier.getPrecioExtra(),
                modifier.getActivo(),
                modifier.getVisiblePublico(),
                modifier.getOrden()
        );
    }

    private CatalogProductModifierGroupResponseDTO toProductModifierGroupResponse(ProductModifierGroup assignment) {
        ModifierGroup group = assignment.getGroup();
        return new CatalogProductModifierGroupResponseDTO(
                assignment.getId(),
                assignment.getProducto().getId(),
                toModifierGroupResponse(group),
                assignment.getRequeridoOverride() != null ? assignment.getRequeridoOverride() : group.getRequerido(),
                assignment.getMinSelectionOverride() != null ? assignment.getMinSelectionOverride() : group.getMinSelection(),
                assignment.getMaxSelectionOverride() != null ? assignment.getMaxSelectionOverride() : group.getMaxSelection(),
                assignment.getOrden()
        );
    }

    private CatalogComboComponentResponseDTO toComboComponentResponse(ComboComponent component) {
        ProductoVariante variant = component.getComponentVariant();
        Producto componentProduct = variant.getProducto();
        return new CatalogComboComponentResponseDTO(
                component.getId(),
                component.getComboProduct().getId(),
                variant.getId(),
                componentProduct.getId(),
                componentProduct.getNombre(),
                variant.getNombre(),
                component.getCantidad(),
                component.getRequerido(),
                component.getReemplazable(),
                component.getOrden()
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

    private String resolveModifierGroupSlug(String requestedSlug, String name) {
        String slug = slugify(StringUtils.hasText(requestedSlug) ? requestedSlug : name);
        if (StringUtils.hasText(requestedSlug) && modifierGroupRepository.existsBySlug(slug)) {
            throw new BusinessException("Ya existe un grupo de modificadores con slug: " + slug);
        }
        return uniqueSlug(slug, modifierGroupRepository::existsBySlug);
    }

    private String resolveModifierSlug(Long groupId, String requestedSlug, String name) {
        String slug = slugify(StringUtils.hasText(requestedSlug) ? requestedSlug : name);
        if (modifierRepository.existsByGroupIdAndSlug(groupId, slug)) {
            throw new BusinessException("Ya existe un modificador con slug en este grupo: " + slug);
        }
        return slug;
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

    private void validateSelection(Integer minSelection, Integer maxSelection) {
        if (minSelection != null && maxSelection != null && minSelection > maxSelection) {
            throw new BusinessException("minSelection no puede ser mayor que maxSelection");
        }
    }

    private void validateComboProduct(Producto product) {
        if (product.getTipoProducto() != ProductoTipo.COMBO && product.getTipoProducto() != ProductoTipo.PROMOCION) {
            throw new BusinessException("Solo productos COMBO o PROMOCION aceptan componentes");
        }
    }

    private interface SlugExists {
        boolean exists(String slug);
    }
}

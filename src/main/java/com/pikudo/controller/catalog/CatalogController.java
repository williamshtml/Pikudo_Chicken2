package com.pikudo.controller.catalog;

import com.pikudo.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.dto.catalog.CatalogCategoryResponseDTO;
import com.pikudo.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageResponseDTO;
import com.pikudo.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.dto.catalog.CatalogProductVariantRequestDTO;
import com.pikudo.dto.catalog.CatalogProductVariantResponseDTO;
import com.pikudo.service.catalog.CatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping("/categories")
    public ResponseEntity<Page<CatalogCategoryResponseDTO>> listCategories(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean publicVisible,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "orden", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(catalogService.listCategories(search, active, publicVisible, available, pageable));
    }

    @PostMapping("/categories")
    public ResponseEntity<CatalogCategoryResponseDTO> createCategory(@Valid @RequestBody CatalogCategoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createCategory(request));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<CatalogProductResponseDTO>> listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Boolean publicVisible,
            @RequestParam(required = false) Boolean available,
            @PageableDefault(size = 20, sort = "orden", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(catalogService.listProducts(search, categoryId, active, publicVisible, available, pageable));
    }

    @PostMapping("/products")
    public ResponseEntity<CatalogProductResponseDTO> createProduct(@Valid @RequestBody CatalogProductRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createProduct(request));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<CatalogProductResponseDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getProduct(id));
    }

    @PostMapping("/products/{id}/variants")
    public ResponseEntity<CatalogProductVariantResponseDTO> addVariant(
            @PathVariable Long id,
            @Valid @RequestBody CatalogProductVariantRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addVariant(id, request));
    }

    @PostMapping(value = "/products/{id}/images", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CatalogProductImageResponseDTO> addImage(
            @PathVariable Long id,
            @Valid @RequestBody CatalogProductImageLinkRequestDTO request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addImage(id, request));
    }

    @PostMapping(value = "/products/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CatalogProductImageResponseDTO> uploadImage(
            @PathVariable Long id,
            @RequestParam(required = false) Long variantId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Boolean primary,
            @RequestParam(required = false) Integer sortOrder,
            @RequestParam(required = false) String altText,
            @RequestParam(required = false) Boolean publicVisible
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(catalogService.uploadImage(id, variantId, file, primary, sortOrder, altText, publicVisible));
    }
}

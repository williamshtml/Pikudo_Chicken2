package com.pikudo.service.catalog;

import com.pikudo.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.dto.catalog.CatalogCategoryResponseDTO;
import com.pikudo.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageResponseDTO;
import com.pikudo.dto.catalog.CatalogProductRequestDTO;
import com.pikudo.dto.catalog.CatalogProductResponseDTO;
import com.pikudo.dto.catalog.CatalogProductVariantRequestDTO;
import com.pikudo.dto.catalog.CatalogProductVariantResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface CatalogService {

    Page<CatalogCategoryResponseDTO> listCategories(String search, Boolean active, Boolean publicVisible, Boolean available, Pageable pageable);

    CatalogCategoryResponseDTO createCategory(CatalogCategoryRequestDTO request);

    Page<CatalogProductResponseDTO> listProducts(String search, Long categoryId, Boolean active, Boolean publicVisible, Boolean available, Pageable pageable);

    CatalogProductResponseDTO createProduct(CatalogProductRequestDTO request);

    CatalogProductResponseDTO getProduct(Long id);

    CatalogProductVariantResponseDTO addVariant(Long productId, CatalogProductVariantRequestDTO request);

    CatalogProductImageResponseDTO addImage(Long productId, CatalogProductImageLinkRequestDTO request);

    CatalogProductImageResponseDTO uploadImage(Long productId, Long variantId, MultipartFile file, Boolean primary, Integer sortOrder, String altText, Boolean publicVisible);
}

package com.pikudo.service.catalog;

import com.pikudo.dto.catalog.CatalogCategoryRequestDTO;
import com.pikudo.dto.catalog.CatalogCategoryResponseDTO;
import com.pikudo.dto.catalog.CatalogComboComponentRequestDTO;
import com.pikudo.dto.catalog.CatalogComboComponentResponseDTO;
import com.pikudo.dto.catalog.CatalogModifierGroupRequestDTO;
import com.pikudo.dto.catalog.CatalogModifierGroupResponseDTO;
import com.pikudo.dto.catalog.CatalogModifierRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageLinkRequestDTO;
import com.pikudo.dto.catalog.CatalogProductImageResponseDTO;
import com.pikudo.dto.catalog.CatalogProductModifierGroupRequestDTO;
import com.pikudo.dto.catalog.CatalogProductModifierGroupResponseDTO;
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

    Page<CatalogModifierGroupResponseDTO> listModifierGroups(String search, Boolean active, Boolean publicVisible, Pageable pageable);

    CatalogModifierGroupResponseDTO createModifierGroup(CatalogModifierGroupRequestDTO request);

    CatalogModifierGroupResponseDTO getModifierGroup(Long id);

    CatalogModifierGroupResponseDTO addModifier(Long groupId, CatalogModifierRequestDTO request);

    java.util.List<CatalogProductModifierGroupResponseDTO> listProductModifierGroups(Long productId);

    CatalogProductModifierGroupResponseDTO assignModifierGroup(Long productId, CatalogProductModifierGroupRequestDTO request);

    void removeModifierGroup(Long productId, Long groupId);

    java.util.List<CatalogComboComponentResponseDTO> listComboComponents(Long productId);

    CatalogComboComponentResponseDTO addComboComponent(Long productId, CatalogComboComponentRequestDTO request);

    void removeComboComponent(Long productId, Long componentId);
}

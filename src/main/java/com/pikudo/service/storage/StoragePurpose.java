package com.pikudo.service.storage;

import java.util.Locale;

public enum StoragePurpose {
    PRODUCT_IMAGE("productos"),
    USER_AVATAR("avatares"),
    DELIVERY_EVIDENCE("delivery-evidence"),
    SUNAT_DOCUMENT("sunat-documents"),
    GENERIC("generic");

    private final String folderName;

    StoragePurpose(String folderName) {
        this.folderName = folderName;
    }

    public String folderName() {
        return folderName;
    }

    public static StoragePurpose fromLegacyFolder(String value) {
        if (value == null || value.isBlank()) {
            return GENERIC;
        }
        String normalized = value.trim().toLowerCase(Locale.ROOT)
                .replace("_", "-")
                .replace(" ", "-");
        return switch (normalized) {
            case "productos", "producto", "products", "product-images" -> PRODUCT_IMAGE;
            case "usuarios", "usuario", "users", "avatar", "avatars", "avatares" -> USER_AVATAR;
            case "delivery", "delivery-evidence", "evidencias-delivery" -> DELIVERY_EVIDENCE;
            case "sunat", "comprobantes", "documentos-sunat" -> SUNAT_DOCUMENT;
            default -> GENERIC;
        };
    }
}

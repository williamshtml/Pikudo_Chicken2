package com.pikudo.restaurant.service.storage;

import com.pikudo.restaurant.entity.Usuario;

import java.io.InputStream;

public record StorageUploadRequest(
        InputStream inputStream,
        String originalFilename,
        String mimeType,
        long expectedSizeBytes,
        StoragePurpose purpose,
        String ownerModule,
        String ownerId,
        Usuario createdBy
) {
}

package com.pikudo.service.storage;

import com.pikudo.entity.Usuario;

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

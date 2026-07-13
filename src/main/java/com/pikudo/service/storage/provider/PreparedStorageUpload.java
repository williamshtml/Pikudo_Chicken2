package com.pikudo.service.storage.provider;

import com.pikudo.service.storage.StoragePurpose;

import java.nio.file.Path;
import java.util.UUID;

public record PreparedStorageUpload(
        UUID id,
        Path tempFile,
        String filename,
        String originalFilename,
        String mimeType,
        String extension,
        long sizeBytes,
        String checksumSha256,
        StoragePurpose purpose
) {
}

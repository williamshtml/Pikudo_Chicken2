package com.pikudo.restaurant.service.storage.provider;

public record StorageProviderResult(
        String provider,
        String bucketOrDrive,
        String folderId,
        String folderPath,
        String externalFileId,
        String publicUrl,
        String downloadUrl
) {
}

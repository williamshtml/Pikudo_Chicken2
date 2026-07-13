package com.pikudo.restaurant.service.storage;

import java.util.UUID;

public record StoredFile(
        UUID id,
        String provider,
        String filename,
        String mimeType,
        long sizeBytes,
        String checksumSha256,
        String contentUrl
) {
}

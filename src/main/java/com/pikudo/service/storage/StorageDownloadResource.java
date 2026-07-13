package com.pikudo.service.storage;

import java.io.InputStream;

public record StorageDownloadResource(
        InputStream inputStream,
        String filename,
        String mimeType,
        long sizeBytes
) {
}

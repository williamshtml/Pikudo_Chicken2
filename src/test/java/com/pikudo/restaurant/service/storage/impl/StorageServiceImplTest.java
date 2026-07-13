package com.pikudo.restaurant.service.storage.impl;

import com.pikudo.restaurant.config.properties.StorageProperties;
import com.pikudo.restaurant.entity.storage.StorageFile;
import com.pikudo.restaurant.repository.storage.StorageFileRepository;
import com.pikudo.restaurant.service.storage.StorageDownloadResource;
import com.pikudo.restaurant.service.storage.StoragePurpose;
import com.pikudo.restaurant.service.storage.StorageUploadRequest;
import com.pikudo.restaurant.service.storage.StoredFile;
import com.pikudo.restaurant.service.storage.provider.LocalStorageProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StorageServiceImplTest {

    @TempDir
    Path tempDir;

    @Test
    void uploadsLocalFileWithChecksumMetadataAndReadableContent() throws IOException {
        StorageProperties properties = new StorageProperties();
        properties.setProvider("local");
        properties.getLocal().setBasePath(tempDir.toString());

        StorageFileRepository repository = mock(StorageFileRepository.class);
        AtomicReference<StorageFile> savedFile = new AtomicReference<>();
        when(repository.save(any(StorageFile.class))).thenAnswer(invocation -> {
            StorageFile file = invocation.getArgument(0);
            savedFile.set(file);
            return file;
        });
        when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(savedFile.get()));

        StorageServiceImpl service = new StorageServiceImpl(
                properties,
                repository,
                List.of(new LocalStorageProvider(properties))
        );

        byte[] bytes = "pikudo-image-content".getBytes();
        StoredFile storedFile = service.upload(new StorageUploadRequest(
                new ByteArrayInputStream(bytes),
                "pollo.png",
                "image/png",
                bytes.length,
                StoragePurpose.PRODUCT_IMAGE,
                "catalog",
                "123",
                null
        ));

        assertThat(storedFile.contentUrl()).isEqualTo("/api/files/" + storedFile.id() + "/content");
        assertThat(storedFile.checksumSha256()).hasSize(64);
        assertThat(savedFile.get().getProvider()).isEqualTo("local");
        assertThat(savedFile.get().getFolderPath()).isEqualTo("productos");
        assertThat(Files.exists(tempDir.resolve(savedFile.get().getExternalFileId()))).isTrue();

        StorageDownloadResource resource = service.open(storedFile.id());
        assertThat(resource.inputStream().readAllBytes()).isEqualTo(bytes);
    }
}

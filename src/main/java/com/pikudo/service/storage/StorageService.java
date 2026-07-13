package com.pikudo.service.storage;

import com.pikudo.entity.storage.StorageFile;

import java.util.Optional;
import java.util.UUID;

public interface StorageService {

    StoredFile upload(StorageUploadRequest request);

    Optional<StorageFile> find(UUID id);

    StorageDownloadResource open(UUID id);

    void delete(UUID id);
}

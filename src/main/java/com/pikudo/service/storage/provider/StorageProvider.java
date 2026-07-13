package com.pikudo.service.storage.provider;

import com.pikudo.entity.storage.StorageFile;
import com.pikudo.service.storage.StorageDownloadResource;

public interface StorageProvider {

    String name();

    boolean isEnabled();

    StorageProviderResult upload(PreparedStorageUpload upload);

    StorageDownloadResource open(StorageFile file);

    void delete(StorageFile file);
}

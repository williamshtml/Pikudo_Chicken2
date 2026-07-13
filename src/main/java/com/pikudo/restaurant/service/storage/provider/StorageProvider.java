package com.pikudo.restaurant.service.storage.provider;

import com.pikudo.restaurant.entity.storage.StorageFile;
import com.pikudo.restaurant.service.storage.StorageDownloadResource;

public interface StorageProvider {

    String name();

    boolean isEnabled();

    StorageProviderResult upload(PreparedStorageUpload upload);

    StorageDownloadResource open(StorageFile file);

    void delete(StorageFile file);
}

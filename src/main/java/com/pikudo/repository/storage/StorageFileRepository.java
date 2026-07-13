package com.pikudo.repository.storage;

import com.pikudo.entity.storage.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StorageFileRepository extends JpaRepository<StorageFile, UUID> {
}

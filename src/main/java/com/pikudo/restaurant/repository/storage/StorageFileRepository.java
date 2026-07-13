package com.pikudo.restaurant.repository.storage;

import com.pikudo.restaurant.entity.storage.StorageFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StorageFileRepository extends JpaRepository<StorageFile, UUID> {
}

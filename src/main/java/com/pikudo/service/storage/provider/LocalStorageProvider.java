package com.pikudo.service.storage.provider;

import com.pikudo.config.properties.StorageProperties;
import com.pikudo.entity.storage.StorageFile;
import com.pikudo.exception.BusinessException;
import com.pikudo.service.storage.StorageDownloadResource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
@RequiredArgsConstructor
public class LocalStorageProvider implements StorageProvider {

    private final StorageProperties properties;

    @Override
    public String name() {
        return "local";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public StorageProviderResult upload(PreparedStorageUpload upload) {
        try {
            Path basePath = Paths.get(properties.getLocal().getBasePath()).normalize();
            Path folder = basePath.resolve(upload.purpose().folderName()).normalize();
            Files.createDirectories(folder);

            Path destination = folder.resolve(upload.filename()).normalize();
            Files.copy(upload.tempFile(), destination, StandardCopyOption.REPLACE_EXISTING);

            String externalPath = basePath.relativize(destination).toString().replace("\\", "/");
            return new StorageProviderResult(
                    name(),
                    null,
                    null,
                    upload.purpose().folderName(),
                    externalPath,
                    null,
                    "/api/files/" + upload.id() + "/content"
            );
        } catch (IOException e) {
            throw new BusinessException("No se pudo guardar el archivo localmente: " + e.getMessage());
        }
    }

    @Override
    public StorageDownloadResource open(StorageFile file) {
        try {
            if (!StringUtils.hasText(file.getExternalFileId())) {
                throw new BusinessException("El archivo local no tiene ruta fisica registrada");
            }
            Path path = Paths.get(properties.getLocal().getBasePath())
                    .resolve(file.getExternalFileId())
                    .normalize();
            if (!Files.exists(path)) {
                throw new BusinessException("El archivo solicitado ya no existe en storage local");
            }
            return new StorageDownloadResource(
                    Files.newInputStream(path),
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : file.getFilename(),
                    file.getMimeType(),
                    file.getSizeBytes()
            );
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo local: " + e.getMessage());
        }
    }

    @Override
    public void delete(StorageFile file) {
        if (!StringUtils.hasText(file.getExternalFileId())) {
            return;
        }
        try {
            Path path = Paths.get(properties.getLocal().getBasePath())
                    .resolve(file.getExternalFileId())
                    .normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            throw new BusinessException("No se pudo eliminar el archivo local: " + e.getMessage());
        }
    }
}

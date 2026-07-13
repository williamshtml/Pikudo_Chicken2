package com.pikudo.service.storage.impl;

import com.pikudo.config.properties.StorageProperties;
import com.pikudo.entity.storage.StorageFile;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.storage.StorageFileRepository;
import com.pikudo.service.storage.StorageDownloadResource;
import com.pikudo.service.storage.StorageService;
import com.pikudo.service.storage.StorageUploadRequest;
import com.pikudo.service.storage.StoredFile;
import com.pikudo.service.storage.provider.PreparedStorageUpload;
import com.pikudo.service.storage.provider.StorageProvider;
import com.pikudo.service.storage.provider.StorageProviderResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServiceImpl implements StorageService {

    private static final long MAX_UPLOAD_BYTES = 20L * 1024L * 1024L;

    private final StorageProperties properties;
    private final StorageFileRepository storageFileRepository;
    private final List<StorageProvider> providers;

    @Override
    @Transactional
    public StoredFile upload(StorageUploadRequest request) {
        if (request == null || request.inputStream() == null) {
            throw new BusinessException("No se recibio archivo para subir");
        }
        UUID id = UUID.randomUUID();
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile("pikudo-upload-", ".tmp");
            HashedCopy hashedCopy = copyToTempFile(request.inputStream(), tempFile);
            if (hashedCopy.sizeBytes() <= 0) {
                throw new BusinessException("El archivo recibido esta vacio");
            }
            if (request.expectedSizeBytes() > 0 && request.expectedSizeBytes() != hashedCopy.sizeBytes()) {
                throw new BusinessException("El tamano recibido no coincide con el tamano real del archivo");
            }
            String originalFilename = cleanFilename(request.originalFilename());
            String extension = extensionFrom(originalFilename);
            String filename = id + (StringUtils.hasText(extension) ? "." + extension : "");

            PreparedStorageUpload prepared = new PreparedStorageUpload(
                    id,
                    tempFile,
                    filename,
                    originalFilename,
                    request.mimeType(),
                    extension,
                    hashedCopy.sizeBytes(),
                    hashedCopy.checksumSha256(),
                    request.purpose()
            );

            StorageProvider provider = selectedProvider();
            StorageProviderResult result = provider.upload(prepared);
            StorageFile entity = StorageFile.builder()
                    .id(id)
                    .provider(result.provider())
                    .bucketOrDrive(result.bucketOrDrive())
                    .folderId(result.folderId())
                    .folderPath(result.folderPath())
                    .externalFileId(result.externalFileId())
                    .publicUrl(result.publicUrl())
                    .downloadUrl(result.downloadUrl())
                    .filename(filename)
                    .originalFilename(originalFilename)
                    .mimeType(request.mimeType())
                    .extension(extension)
                    .sizeBytes(hashedCopy.sizeBytes())
                    .checksumSha256(hashedCopy.checksumSha256())
                    .ownerModule(request.ownerModule())
                    .ownerId(request.ownerId())
                    .createdBy(request.createdBy())
                    .createdAt(Instant.now())
                    .build();
            StorageFile saved = storageFileRepository.save(entity);
            return new StoredFile(
                    saved.getId(),
                    saved.getProvider(),
                    saved.getFilename(),
                    saved.getMimeType(),
                    saved.getSizeBytes(),
                    saved.getChecksumSha256(),
                    saved.getDownloadUrl()
            );
        } catch (IOException e) {
            throw new BusinessException("No se pudo procesar el archivo: " + e.getMessage());
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException ignored) {
                    // El temporal no debe bloquear el flujo principal.
                }
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<StorageFile> find(UUID id) {
        return storageFileRepository.findById(id)
                .filter(file -> file.getDeletedAt() == null);
    }

    @Override
    @Transactional(readOnly = true)
    public StorageDownloadResource open(UUID id) {
        StorageFile file = find(id)
                .orElseThrow(() -> new BusinessException("Archivo no encontrado"));
        return providerByName(file.getProvider()).open(file);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        StorageFile file = find(id)
                .orElseThrow(() -> new BusinessException("Archivo no encontrado"));
        providerByName(file.getProvider()).delete(file);
        file.setDeletedAt(Instant.now());
        storageFileRepository.save(file);
    }

    private HashedCopy copyToTempFile(InputStream inputStream, Path tempFile) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long total = 0;
            byte[] buffer = new byte[8192];
            try (InputStream source = inputStream;
                 DigestInputStream digestInputStream = new DigestInputStream(source, digest);
                 var output = Files.newOutputStream(tempFile)) {
                int read;
                while ((read = digestInputStream.read(buffer)) != -1) {
                    total += read;
                    if (total > MAX_UPLOAD_BYTES) {
                        throw new BusinessException("El archivo supera el tamano maximo permitido (20 MB)");
                    }
                    output.write(buffer, 0, read);
                }
            }
            return new HashedCopy(total, HexFormat.of().formatHex(digest.digest()));
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("No se pudo calcular checksum SHA-256");
        }
    }

    private StorageProvider selectedProvider() {
        String desired = properties.getProvider();
        if ("google-drive".equalsIgnoreCase(desired) && !properties.getGoogleDrive().isEnabled()) {
            desired = "local";
        }
        return providerByName(desired);
    }

    private StorageProvider providerByName(String name) {
        return providers.stream()
                .filter(StorageProvider::isEnabled)
                .filter(provider -> provider.name().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Storage provider no disponible: " + name));
    }

    private String cleanFilename(String filename) {
        if (!StringUtils.hasText(filename)) {
            return "archivo";
        }
        return Path.of(filename).getFileName().toString().replaceAll("[\\r\\n]", "").trim();
    }

    private String extensionFrom(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) {
            return "";
        }
        return filename.substring(dot + 1).toLowerCase();
    }

    private record HashedCopy(long sizeBytes, String checksumSha256) {
    }
}

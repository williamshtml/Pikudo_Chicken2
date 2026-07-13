package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.service.archivo.ArchivoService;
import com.pikudo.restaurant.service.storage.StoragePurpose;
import com.pikudo.restaurant.service.storage.StorageService;
import com.pikudo.restaurant.service.storage.StorageUploadRequest;
import com.pikudo.restaurant.service.storage.StoredFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ArchivoServiceImpl implements ArchivoService {

    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    private final StorageService storageService;

    @Override
    public String subir(MultipartFile archivo, String subcarpeta) {
        validarImagen(archivo);

        try {
            StoragePurpose purpose = StoragePurpose.fromLegacyFolder(subcarpeta);
            StoredFile storedFile = storageService.upload(new StorageUploadRequest(
                    archivo.getInputStream(),
                    StringUtils.cleanPath(archivo.getOriginalFilename() == null ? "imagen" : archivo.getOriginalFilename()),
                    archivo.getContentType() != null ? archivo.getContentType() : "application/octet-stream",
                    archivo.getSize(),
                    purpose,
                    purpose.folderName(),
                    null,
                    null
            ));
            log.info("Archivo guardado en storage {}: {}", storedFile.provider(), storedFile.contentUrl());
            return storedFile.contentUrl();
        } catch (IOException e) {
            throw new BusinessException("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    @Override
    public void validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("No se selecciono ningun archivo");
        }

        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new BusinessException("El archivo supera el tamano maximo permitido (5 MB)");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = extraerExtension(nombreOriginal);

        if (!EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new BusinessException("Formato de imagen no permitido. Usa: " + String.join(", ", EXTENSIONES_PERMITIDAS));
        }
    }

    @Override
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }

        UUID storageId = extraerStorageId(rutaRelativa);
        if (storageId != null) {
            storageService.delete(storageId);
            return;
        }

        try {
            Path ruta = Path.of(rutaRelativa.startsWith("/") ? rutaRelativa.substring(1) : rutaRelativa);
            Files.deleteIfExists(ruta);
            log.info("Archivo legacy eliminado: {}", rutaRelativa);
        } catch (IOException e) {
            log.warn("No se pudo eliminar el archivo legacy {}: {}", rutaRelativa, e.getMessage());
        }
    }

    private UUID extraerStorageId(String rutaRelativa) {
        String marker = "/api/files/";
        int start = rutaRelativa.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int idStart = start + marker.length();
        int idEnd = rutaRelativa.indexOf("/content", idStart);
        if (idEnd < 0) {
            return null;
        }
        try {
            return UUID.fromString(rutaRelativa.substring(idStart, idEnd));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String extraerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            throw new BusinessException("El archivo no tiene una extension valida");
        }
        List<String> partes = List.of(nombreArchivo.split("\\."));
        return partes.get(partes.size() - 1);
    }
}

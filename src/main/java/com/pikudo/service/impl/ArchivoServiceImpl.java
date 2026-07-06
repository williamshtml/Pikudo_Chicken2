package com.pikudo.service.impl;

import com.pikudo.exception.BusinessException;
import com.pikudo.service.archivo.ArchivoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class ArchivoServiceImpl implements ArchivoService {

    private static final String CARPETA_BASE = "uploads";
    private static final long TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024; // 5 MB
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");

    @Override
    public String subir(MultipartFile archivo, String subcarpeta) {
        validarImagen(archivo);

        try {
            Path directorio = Paths.get(CARPETA_BASE, subcarpeta);
            if (!Files.exists(directorio)) {
                Files.createDirectories(directorio);
            }

            String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
            String extension = extraerExtension(nombreOriginal);
            String nombreUnico = UUID.randomUUID() + "." + extension;

            Path destino = directorio.resolve(nombreUnico);
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);

            String rutaRelativa = "/" + CARPETA_BASE + "/" + subcarpeta + "/" + nombreUnico;
            log.info("Archivo guardado: {}", rutaRelativa);
            return rutaRelativa;

        } catch (IOException e) {
            log.error("Error al guardar archivo: {}", e.getMessage());
            throw new BusinessException("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    @Override
    public void validarImagen(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BusinessException("No se seleccionó ningún archivo");
        }

        if (archivo.getSize() > TAMANO_MAXIMO_BYTES) {
            throw new BusinessException("El archivo supera el tamaño máximo permitido (5 MB)");
        }

        String nombreOriginal = archivo.getOriginalFilename();
        String extension = extraerExtension(nombreOriginal);

        if (!EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new BusinessException("Formato de imagen no permitido. Usa: " + String.join(", ", EXTENSIONES_PERMITIDAS));
        }
    }

    @Override
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) return;

        try {
            // rutaRelativa viene como "/uploads/productos/archivo.jpg", se quita el "/" inicial
            Path ruta = Paths.get(rutaRelativa.startsWith("/") ? rutaRelativa.substring(1) : rutaRelativa);
            Files.deleteIfExists(ruta);
            log.info("Archivo eliminado: {}", rutaRelativa);
        } catch (IOException e) {
            // No se relanza: si falla el borrado del archivo fisico, no debe romper
            // la operacion de negocio (ej. actualizar el producto igual debe funcionar)
            log.warn("No se pudo eliminar el archivo {}: {}", rutaRelativa, e.getMessage());
        }
    }

    private String extraerExtension(String nombreArchivo) {
        if (nombreArchivo == null || !nombreArchivo.contains(".")) {
            throw new BusinessException("El archivo no tiene una extensión válida");
        }
        List<String> partes = List.of(nombreArchivo.split("\\."));
        return partes.get(partes.size() - 1);
    }
}
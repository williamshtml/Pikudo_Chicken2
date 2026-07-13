package com.pikudo.restaurant.controller;

import com.pikudo.restaurant.service.storage.StorageDownloadResource;
import com.pikudo.restaurant.service.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final StorageService storageService;

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> content(@PathVariable UUID id) {
        StorageDownloadResource resource = storageService.open(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.mimeType()))
                .contentLength(resource.sizeBytes())
                .cacheControl(CacheControl.noCache())
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(resource.filename())
                                .build()
                                .toString()
                )
                .body(new InputStreamResource(resource.inputStream()));
    }
}

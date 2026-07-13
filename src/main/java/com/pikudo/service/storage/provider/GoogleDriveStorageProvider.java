package com.pikudo.service.storage.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pikudo.config.properties.StorageProperties;
import com.pikudo.entity.storage.StorageFile;
import com.pikudo.exception.BusinessException;
import com.pikudo.service.storage.StorageDownloadResource;
import com.pikudo.service.storage.StoragePurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GoogleDriveStorageProvider implements StorageProvider {

    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String UPLOAD_URL = "https://www.googleapis.com/upload/drive/v3/files";
    private static final String FILE_URL = "https://www.googleapis.com/drive/v3/files/{fileId}";

    private final StorageProperties properties;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String name() {
        return "google-drive";
    }

    @Override
    public boolean isEnabled() {
        return properties.getGoogleDrive().isEnabled();
    }

    @Override
    public StorageProviderResult upload(PreparedStorageUpload upload) {
        String folderId = folderIdFor(upload.purpose());
        try {
            String accessToken = fetchAccessToken();
            String uploadUrl = UriComponentsBuilder.fromUriString(UPLOAD_URL)
                    .queryParam("uploadType", "multipart")
                    .queryParam("fields", "id,name,webViewLink,webContentLink")
                    .build()
                    .toUriString();

            HttpHeaders headers = bearerHeaders(accessToken);
            headers.setContentType(MediaType.MULTIPART_RELATED);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("metadata", metadataPart(upload, folderId));
            body.add("file", filePart(upload));

            ResponseEntity<String> response = restTemplate.exchange(
                    uploadUrl,
                    HttpMethod.POST,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            JsonNode node = objectMapper.readTree(response.getBody());
            String fileId = node.path("id").asText();
            return new StorageProviderResult(
                    name(),
                    "google-drive",
                    folderId,
                    upload.purpose().folderName(),
                    fileId,
                    textOrNull(node, "webViewLink"),
                    "/api/files/" + upload.id() + "/content"
            );
        } catch (IOException | RestClientException e) {
            throw new BusinessException("No se pudo subir el archivo a Google Drive: " + e.getMessage());
        }
    }

    @Override
    public StorageDownloadResource open(StorageFile file) {
        try {
            String accessToken = fetchAccessToken();
            String url = UriComponentsBuilder.fromUriString(FILE_URL)
                    .queryParam("alt", "media")
                    .build(file.getExternalFileId())
                    .toString();

            ResponseEntity<byte[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(bearerHeaders(accessToken)),
                    byte[].class
            );
            byte[] body = response.getBody() != null ? response.getBody() : new byte[0];
            return new StorageDownloadResource(
                    new ByteArrayInputStream(body),
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : file.getFilename(),
                    file.getMimeType(),
                    file.getSizeBytes()
            );
        } catch (RestClientException e) {
            throw new BusinessException("No se pudo leer el archivo desde Google Drive: " + e.getMessage());
        }
    }

    @Override
    public void delete(StorageFile file) {
        if (!StringUtils.hasText(file.getExternalFileId())) {
            return;
        }
        try {
            String accessToken = fetchAccessToken();
            restTemplate.exchange(
                    FILE_URL,
                    HttpMethod.DELETE,
                    new HttpEntity<>(bearerHeaders(accessToken)),
                    Void.class,
                    file.getExternalFileId()
            );
        } catch (RestClientException e) {
            throw new BusinessException("No se pudo eliminar el archivo en Google Drive: " + e.getMessage());
        }
    }

    private String fetchAccessToken() {
        StorageProperties.GoogleDrive drive = properties.getGoogleDrive();
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", drive.getOauthClientId());
        body.add("client_secret", drive.getOauthClientSecret());
        body.add("refresh_token", drive.getOauthRefreshToken());
        body.add("grant_type", "refresh_token");

        ResponseEntity<String> response = restTemplate.postForEntity(TOKEN_URL, body, String.class);
        try {
            String accessToken = objectMapper.readTree(response.getBody()).path("access_token").asText();
            if (!StringUtils.hasText(accessToken)) {
                throw new BusinessException("Google OAuth no devolvio access_token");
            }
            return accessToken;
        } catch (IOException e) {
            throw new BusinessException("No se pudo interpretar la respuesta OAuth de Google: " + e.getMessage());
        }
    }

    private HttpEntity<String> metadataPart(PreparedStorageUpload upload, String folderId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String json = objectMapper.writeValueAsString(Map.of(
                "name", upload.filename(),
                "mimeType", upload.mimeType(),
                "parents", List.of(folderId)
        ));
        return new HttpEntity<>(json, headers);
    }

    private HttpEntity<FileSystemResource> filePart(PreparedStorageUpload upload) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(upload.mimeType()));
        FileSystemResource resource = new FileSystemResource(upload.tempFile()) {
            @Override
            public String getFilename() {
                return upload.filename();
            }
        };
        return new HttpEntity<>(resource, headers);
    }

    private HttpHeaders bearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private String folderIdFor(StoragePurpose purpose) {
        StorageProperties.Folders folders = properties.getGoogleDrive().getFolders();
        String folderId = switch (purpose) {
            case PRODUCT_IMAGE -> folders.getProducts();
            case USER_AVATAR -> folders.getAvatarUsers();
            case DELIVERY_EVIDENCE -> folders.getDeliveryEvidence();
            case SUNAT_DOCUMENT -> folders.getSunatRoot();
            case GENERIC -> folders.getProducts();
        };
        if (!StringUtils.hasText(folderId)) {
            throw new BusinessException("No hay folder de Google Drive configurado para " + purpose.name());
        }
        return folderId;
    }

    private String textOrNull(JsonNode node, String field) {
        return node.hasNonNull(field) ? node.get(field).asText() : null;
    }
}

package com.pikudo.restaurant.entity.storage;

import com.pikudo.restaurant.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "storage_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StorageFile {

    @Id
    private UUID id;

    @Column(nullable = false, length = 30)
    private String provider;

    @Column(name = "bucket_or_drive", length = 160)
    private String bucketOrDrive;

    @Column(name = "folder_id", length = 160)
    private String folderId;

    @Column(name = "folder_path", length = 500)
    private String folderPath;

    @Column(name = "external_file_id", length = 240)
    private String externalFileId;

    @Column(name = "public_url")
    private String publicUrl;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(nullable = false, length = 255)
    private String filename;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "mime_type", nullable = false, length = 120)
    private String mimeType;

    @Column(length = 20)
    private String extension;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Column(name = "owner_module", length = 60)
    private String ownerModule;

    @Column(name = "owner_id", length = 80)
    private String ownerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_usuario_id")
    private Usuario createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}

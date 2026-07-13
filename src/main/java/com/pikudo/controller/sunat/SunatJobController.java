package com.pikudo.controller.sunat;

import com.pikudo.dto.sunat.SunatJobResponseDTO;
import com.pikudo.entity.sunat.SunatSubmissionJob;
import com.pikudo.entity.sunat.SunatSubmissionStatus;
import com.pikudo.service.sunat.SunatSubmissionJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sunat/jobs")
@RequiredArgsConstructor
public class SunatJobController {

    private final SunatSubmissionJobService service;

    @GetMapping
    public ResponseEntity<List<SunatJobResponseDTO>> list(@RequestParam(required = false) SunatSubmissionStatus status) {
        return ResponseEntity.ok(service.list(status).stream().map(this::toResponse).toList());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<SunatJobResponseDTO> retry(@PathVariable UUID id) {
        return ResponseEntity.ok(toResponse(service.retry(id)));
    }

    @PostMapping("/process-pending")
    public ResponseEntity<Map<String, Integer>> processPending() {
        return ResponseEntity.ok(Map.of("processed", service.processPending()));
    }

    private SunatJobResponseDTO toResponse(SunatSubmissionJob job) {
        return SunatJobResponseDTO.builder()
                .id(job.getId())
                .comprobanteId(job.getComprobante() != null ? job.getComprobante().getId() : null)
                .notaCreditoId(job.getNotaCredito() != null ? job.getNotaCredito().getId() : null)
                .notaDebitoId(job.getNotaDebito() != null ? job.getNotaDebito().getId() : null)
                .documentType(job.getDocumentType())
                .status(job.getStatus())
                .attempts(job.getAttempts())
                .nextRetryAt(job.getNextRetryAt())
                .lastError(job.getLastError())
                .responseCode(job.getSunatResponseCode())
                .responseDescription(job.getSunatResponseDescription())
                .sentAt(job.getSunatSentAt())
                .acceptedAt(job.getSunatAcceptedAt())
                .fechaCreacion(job.getFechaCreacion())
                .build();
    }
}

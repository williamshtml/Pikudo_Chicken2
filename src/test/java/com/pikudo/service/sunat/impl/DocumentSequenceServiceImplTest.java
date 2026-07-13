package com.pikudo.service.sunat.impl;

import com.pikudo.entity.TipoComprobante;
import com.pikudo.entity.sunat.DocumentSequence;
import com.pikudo.repository.sunat.DocumentSequenceRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DocumentSequenceServiceImplTest {

    @Test
    void returnsCurrentNumberAndIncrementsSequence() {
        DocumentSequenceRepository repository = mock(DocumentSequenceRepository.class);
        DocumentSequence sequence = new DocumentSequence();
        sequence.setDocumentType("FACTURA");
        sequence.setSerie("F001");
        sequence.setNextNumber(15L);
        when(repository.findByDocumentTypeAndSerie("FACTURA", "F001")).thenReturn(Optional.of(sequence));

        DocumentSequenceServiceImpl service = new DocumentSequenceServiceImpl(repository);

        String correlativo = service.nextCorrelativo(TipoComprobante.FACTURA, "F001");

        assertThat(correlativo).isEqualTo("00000015");
        assertThat(sequence.getNextNumber()).isEqualTo(16L);
        verify(repository).save(sequence);
    }
}

package com.pikudo.service.sunat.impl;

import com.pikudo.entity.TipoComprobante;
import com.pikudo.entity.sunat.DocumentSequence;
import com.pikudo.exception.BusinessException;
import com.pikudo.repository.sunat.DocumentSequenceRepository;
import com.pikudo.service.sunat.DocumentSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentSequenceServiceImpl implements DocumentSequenceService {

    private final DocumentSequenceRepository repository;

    @Override
    @Transactional
    public String nextCorrelativo(TipoComprobante documentType, String serie) {
        DocumentSequence sequence = repository.findByDocumentTypeAndSerie(documentType.name(), serie)
                .orElseThrow(() -> new BusinessException("No existe secuencia para " + documentType + " serie " + serie));
        long current = sequence.getNextNumber();
        sequence.setNextNumber(current + 1);
        repository.save(sequence);
        return String.format("%08d", current);
    }
}

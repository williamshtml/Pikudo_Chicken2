package com.pikudo.service.impl;

import com.pikudo.service.MesaService;
import com.pikudo.dto.mesa.MesaRequestDTO;
import com.pikudo.dto.mesa.MesaResponseDTO;
import com.pikudo.entity.Mesa;
import com.pikudo.mapper.MesaMapper;
import com.pikudo.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MesaServiceImpl implements MesaService {

    private final MesaRepository mesaRepository;
    private final MesaMapper mesaMapper; // Inyectamos el nuevo mapper aquí

    @Override
    @Transactional
    public MesaResponseDTO crear(MesaRequestDTO dto) {
        Mesa mesa = Mesa.builder()
                .numero(dto.getNumero())
                .capacidad(dto.getCapacidad())
                .estado(true) // Siempre es buena idea que nazca activa
                .build();
        return mesaMapper.toDTO(mesaRepository.save(mesa));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listarTodas() {
        return mesaRepository.findAll().stream()
                .map(mesaMapper::toDTO) // Se ve mucho más limpio así
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MesaResponseDTO> listarDisponibles() {
        return mesaRepository.findByEstado(true).stream()
                .map(mesaMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MesaResponseDTO buscarPorId(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        return mesaMapper.toDTO(mesa);
    }

    @Override
    @Transactional
    public MesaResponseDTO actualizar(Long id, MesaRequestDTO dto) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad());
        return mesaMapper.toDTO(mesaRepository.save(mesa));
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        mesa.setEstado(false);
        mesaRepository.save(mesa);
    }
}

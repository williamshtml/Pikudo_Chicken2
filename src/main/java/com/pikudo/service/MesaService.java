package com.pikudo.service;

import com.pikudo.dto.mesa.MesaRequestDTO;
import com.pikudo.dto.mesa.MesaResponseDTO;
import com.pikudo.entity.Mesa;
import com.pikudo.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)

public class MesaService {

    private final MesaRepository mesaRepository;

    // ─── CREAR ────────────────────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public MesaResponseDTO crear(MesaRequestDTO dto) {
        Mesa mesa = Mesa.builder()
                .numero(dto.getNumero())
                .capacidad(dto.getCapacidad())
                .build();
        return toDTO(mesaRepository.save(mesa));
    }

    // ─── LISTAR TODAS ─────────────────────────────────────────────────────────
    public List<MesaResponseDTO> listarTodas() {
        return mesaRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── LISTAR SOLO DISPONIBLES (estado = true) ──────────────────────────────
    public List<MesaResponseDTO> listarDisponibles() {
        return mesaRepository.findByEstado(true)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public MesaResponseDTO buscarPorId(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        return toDTO(mesa);
    }

    // ─── ACTUALIZAR ───────────────────────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public MesaResponseDTO actualizar(Long id, MesaRequestDTO dto) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        mesa.setNumero(dto.getNumero());
        mesa.setCapacidad(dto.getCapacidad());
        return toDTO(mesaRepository.save(mesa));
    }

    // ─── DESACTIVAR (estado = false) ──────────────────────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public void desactivar(Long id) {
        Mesa mesa = mesaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mesa no encontrada con id: " + id));
        mesa.setEstado(false);
        mesaRepository.save(mesa);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private MesaResponseDTO toDTO(Mesa m) {
        return new MesaResponseDTO(
                m.getId(),
                m.getNumero(),
                m.getCapacidad(),
                Boolean.TRUE.equals(m.getEstado()) ? "DISPONIBLE" : "INACTIVA"
        );
    }
}

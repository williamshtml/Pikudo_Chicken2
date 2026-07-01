package com.pikudo.service;

import com.pikudo.dto.mesa.MesaRequestDTO;
import com.pikudo.dto.mesa.MesaResponseDTO;
import java.util.List;

public interface MesaService {
    MesaResponseDTO crear(MesaRequestDTO dto);
    List<MesaResponseDTO> listarTodas();
    List<MesaResponseDTO> listarDisponibles();
    MesaResponseDTO buscarPorId(Long id);
    MesaResponseDTO actualizar(Long id, MesaRequestDTO dto);
    void desactivar(Long id);
}

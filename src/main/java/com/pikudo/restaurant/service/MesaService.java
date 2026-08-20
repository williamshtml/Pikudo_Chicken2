package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.mesa.MesaEstadoResponseDTO;
import com.pikudo.restaurant.dto.mesa.MesaRequestDTO;
import com.pikudo.restaurant.dto.mesa.MesaResponseDTO;
import java.util.List;

public interface MesaService {
    MesaResponseDTO crear(MesaRequestDTO dto);
    List<MesaResponseDTO> listarTodas();
    List<MesaResponseDTO> listarDisponibles();
    List<MesaEstadoResponseDTO> listarConOcupacion();
    MesaResponseDTO buscarPorId(Long id);
    MesaResponseDTO actualizar(Long id, MesaRequestDTO dto);
    MesaResponseDTO actualizarSalon(Long id, Integer salon);
    void desactivar(Long id);
}
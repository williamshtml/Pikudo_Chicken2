package com.pikudo.restaurant.service;
import com.pikudo.restaurant.dto.mesa.MesaEstadoResponseDTO;
import com.pikudo.restaurant.dto.mesa.MesaRequestDTO;
import com.pikudo.restaurant.dto.mesa.MesaResponseDTO;
import java.util.List;
public interface MesaService {
    MesaResponseDTO crear(MesaRequestDTO dto);
    List<MesaResponseDTO> listarTodas();
    List<MesaResponseDTO> listarDisponibles();
    MesaResponseDTO buscarPorId(Long id);
    MesaResponseDTO actualizar(Long id, MesaRequestDTO dto);
    void desactivar(Long id);

    // Nuevo: la vista real para el salon del restaurante. Combina si la mesa
    // esta activa en el catalogo Y si tiene un pedido abierto ahora mismo
    // (calculado, no un campo fijo que se pueda desincronizar).
    List<MesaEstadoResponseDTO> listarConOcupacion();
}
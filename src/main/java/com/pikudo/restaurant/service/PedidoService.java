package com.pikudo.restaurant.service;

import com.pikudo.restaurant.dto.pedido.PedidoRequestDTO;
import com.pikudo.restaurant.dto.pedido.PedidoResponseDTO;
import com.pikudo.restaurant.entity.EstadoPedido;
import java.util.List;

public interface PedidoService {
    PedidoResponseDTO crear(PedidoRequestDTO dto);
    PedidoResponseDTO tomarPedido(Long id); 
    List<PedidoResponseDTO> listarTodos();
    List<PedidoResponseDTO> listarPorEstado(EstadoPedido estado);
    List<PedidoResponseDTO> listarAbiertosPorMesa(Long mesaId, EstadoPedido estadoExcluido);
    PedidoResponseDTO buscarPorId(Long id);
    PedidoResponseDTO cambiarEstado(Long id, EstadoPedido nuevoEstado);
    void cancelar(Long id);
}
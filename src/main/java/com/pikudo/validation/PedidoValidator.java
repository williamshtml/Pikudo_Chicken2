/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.validation;

/**
 * Validador de reglas de negocio para Pedido.
 * Verifica que la mesa exista, que los productos pedidos estén activos
 * y tengan stock suficiente antes de registrar la comanda.
 * Es invocado desde la capa service antes de crear un nuevo pedido.
 * Agregado por: [tu nombre] - Módulo de validaciones.
 */

import com.pikudo.dto.pedido.PedidoRequestDTO;
import com.pikudo.entity.Producto;
import com.pikudo.exception.BusinessException;
import com.pikudo.exception.ResourceNotFoundException;
import com.pikudo.repository.MesaRepository;
import com.pikudo.repository.ProductoRepository;
import org.springframework.stereotype.Component;

@Component
public class PedidoValidator {

    private final MesaRepository mesaRepository;
    private final ProductoRepository productoRepository;

    public PedidoValidator(MesaRepository mesaRepository, ProductoRepository productoRepository) {
        this.mesaRepository = mesaRepository;
        this.productoRepository = productoRepository;
    }

    // Valida los datos antes de crear un nuevo pedido
    public void validarParaCrear(PedidoRequestDTO dto) {
        validarMesaExistente(dto.getMesaId());

        if (dto.getDetalles() == null || dto.getDetalles().isEmpty()) {
            throw new BusinessException("El pedido debe tener al menos un producto");
        }

        for (PedidoRequestDTO.DetalleItemDTO item : dto.getDetalles()) {
            validarProductoDisponible(item.getProductoId(), item.getCantidad());
        }
    }

    // Verifica que la mesa exista en base de datos (findById viene incluido en JpaRepository)
    private void validarMesaExistente(Long mesaId) {
        mesaRepository.findById(mesaId)
                .orElseThrow(() -> new ResourceNotFoundException("La mesa indicada no existe"));
    }

    // Verifica que el producto exista, esté activo y tenga stock suficiente para la cantidad pedida
    private void validarProductoDisponible(Long productoId, Integer cantidadSolicitada) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ResourceNotFoundException("El producto con id " + productoId + " no existe"));

        if (!producto.getEstado()) {
            throw new BusinessException("El producto '" + producto.getNombre() + "' no está disponible");
        }

        if (producto.getStock() < cantidadSolicitada) {
            throw new BusinessException("Stock insuficiente para '" + producto.getNombre() +
                    "'. Disponible: " + producto.getStock() + ", solicitado: " + cantidadSolicitada);
        }
    }
}

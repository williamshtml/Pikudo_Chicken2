/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.pikudo.service;

import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.ComprobanteRepository;
import com.pikudo.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;

    // IGV peruano vigente
    private static final BigDecimal IGV_RATE = new BigDecimal("0.18");

    // ─── EMITIR COMPROBANTE ───────────────────────────────────────────────────
    @Transactional
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + dto.getPedidoId()));

        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("Este pedido ya tiene un comprobante emitido");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELLED) {
            throw new RuntimeException("No se puede emitir comprobante de un pedido cancelado");
        }

        TipoComprobante tipo = TipoComprobante.valueOf(dto.getTipoComprobante());

        // Validar que si es FACTURA venga el RUC y razón social
        if (tipo == TipoComprobante.FACTURA) {
            if (dto.getRuc() == null || dto.getRuc().isBlank()) {
                throw new RuntimeException("El RUC es obligatorio para emitir una factura");
            }
            if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
                throw new RuntimeException("La razón social es obligatoria para emitir una factura");
            }
        }

        // Calcular montos
        BigDecimal total     = pedido.getTotal();
        BigDecimal igv       = total.multiply(IGV_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal montoNeto = total.subtract(igv).setScale(2, RoundingMode.HALF_UP);

        // Generar serie y correlativo según tipo
        String serie       = generarSerie(tipo);
        String correlativo = generarCorrelativo(serie);

        Comprobante comprobante = Comprobante.builder()
                .pedido(pedido)
                .tipoComprobante(tipo)
                .serie(serie)
                .correlativo(correlativo)
                .metodo_pago(dto.getMetodoPago())
                .montoNeto(montoNeto)
                .igv(igv)
                .montoTotal(total)
                .ruc(dto.getRuc())
                .razonSocial(dto.getRazonSocial())
                .build();

        // Marcar pedido como PAGADO
        pedido.setEstado(EstadoPedido.PAID);
        pedido.setTipoComprobante(tipo);
        pedidoRepository.save(pedido);

        return toDTO(comprobanteRepository.save(comprobante));
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public ComprobanteResponseDTO buscarPorId(Long id) {
        Comprobante c = comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con id: " + id));
        return toDTO(c);
    }

    // ─── LISTAR TODOS ─────────────────────────────────────────────────────────
    public List<ComprobanteResponseDTO> listarTodos() {
        return comprobanteRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─── HELPERS PRIVADOS ─────────────────────────────────────────────────────
    private String generarSerie(TipoComprobante tipo) {
        return switch (tipo) {
            case FACTURA        -> "F001";
            case BOLETA         -> "B001";
            case TICKET_INTERNO -> "T001";
        };
    }

    private String generarCorrelativo(String serie) {
        // Cuenta cuántos comprobantes ya existen con esa serie y genera el siguiente número
        long count = comprobanteRepository.findAll()
                .stream()
                .filter(c -> c.getSerie().equals(serie))
                .count();
        return String.format("%08d", count + 1); // Ej: "00000125"
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private ComprobanteResponseDTO toDTO(Comprobante c) {
        return new ComprobanteResponseDTO(
                c.getId(),
                c.getPedido().getId(),
                c.getTipoComprobante().name(),
                c.getSerie(),
                Integer.parseInt(c.getCorrelativo()),
                c.getMetodo_pago(),
                c.getMontoNeto(),
                c.getIgv(),
                c.getMontoTotal(),
                c.getRuc(),
                c.getRazonSocial(),
                c.getFechaEmision()
        );
    }
}
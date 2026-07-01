package com.pikudo.sevice.impl;

import com.pikudo.service.ComprobanteService;
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

@Service
@RequiredArgsConstructor
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;

    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    @Override
    @Transactional
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + dto.getPedidoId()));

        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("El pedido ya tiene un comprobante emitido");
        }

        TipoComprobante tipo = TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase());

        // Validaciones para FACTURA
        if (tipo == TipoComprobante.FACTURA) {
            if (dto.getRuc() == null || dto.getRuc().isBlank()) throw new RuntimeException("El RUC es obligatorio para factura");
            if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) throw new RuntimeException("Razón social obligatoria para factura");
        }

        BigDecimal montoTotal = pedido.getTotal();
        BigDecimal montoNeto = montoTotal.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoTotal.subtract(montoNeto);

        String serie = (tipo == TipoComprobante.FACTURA) ? "F001" : "B001";
        String correlativo = String.format("%08d", comprobanteRepository.count() + 1);

        Comprobante comprobante = Comprobante.builder()
                .pedido(pedido)
                .tipoComprobante(tipo)
                .serie(serie)
                .correlativo(correlativo)
                .metodo_pago(dto.getMetodoPago())
                .montoNeto(montoNeto)
                .igv(igv)
                .montoTotal(montoTotal)
                .ruc(dto.getRuc())
                .razonSocial(dto.getRazonSocial())
                .build();

        Comprobante guardado = comprobanteRepository.save(comprobante);

        pedido.setEstado(EstadoPedido.PAID);
        pedido.setTipoComprobante(tipo);
        pedidoRepository.save(pedido);

        return toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponseDTO buscarPorId(Long id) {
        Comprobante c = comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con id: " + id));
        return toDTO(c);
    }

    private ComprobanteResponseDTO toDTO(Comprobante c) {
        ComprobanteResponseDTO r = new ComprobanteResponseDTO();
        r.setId(c.getId());
        r.setPedidoId(c.getPedido() != null ? c.getPedido().getId() : null);
        r.setTipoComprobante(c.getTipoComprobante() != null ? c.getTipoComprobante().name() : null);
        r.setSerie(c.getSerie());
        r.setNumeroCorrelativo(Integer.parseInt(c.getCorrelativo()));
        r.setMetodoPago(c.getMetodo_pago());
        r.setSubtotal(c.getMontoNeto());
        r.setIgv(c.getIgv());
        r.setTotal(c.getMontoTotal());
        r.setRuc(c.getRuc());
        r.setRazonSocial(c.getRazonSocial());
        r.setFechaEmision(c.getFechaEmision());

        // Lógica para Cajero y Mesero
        if (c.getPedido() != null) {
            // El cajero es el usuario que procesa el pedido en ese momento
            if (c.getPedido().getCajero() != null) {
                r.setNombreCajero(c.getPedido().getCajero().getUsername());
            }
            // El mesero solo se asigna si existió atención en mesa
            if (c.getPedido().getMesero() != null) {
                r.setNombreMesero(c.getPedido().getMesero().getUsername());
            } else {
                r.setNombreMesero(null); // El frontend lo detecta como vacío y no imprime la línea
            }
        }
        return r;
    }
}

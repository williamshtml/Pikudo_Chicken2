package com.pikudo.service;

import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.EstadoPedido;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.TipoComprobante;
import com.pikudo.repository.ComprobanteRepository;
import com.pikudo.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;

    // Tasa de IGV en Perú (18%)
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    // ─── EMITIR COMPROBANTE (cierra y cobra el pedido) ────────────────────────
    @Transactional(rollbackFor = Exception.class)
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con id: " + dto.getPedidoId()));

        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("El pedido ya tiene un comprobante emitido");
        }
        if (pedido.getEstado() == EstadoPedido.CANCELLED) {
            throw new RuntimeException("No se puede emitir comprobante de un pedido cancelado");
        }

        TipoComprobante tipo = TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase());

        // FACTURA exige datos de la empresa cliente
        if (tipo == TipoComprobante.FACTURA) {
            if (dto.getRuc() == null || dto.getRuc().isBlank()) {
                throw new RuntimeException("El RUC es obligatorio para emitir una factura");
            }
            if (dto.getRazonSocial() == null || dto.getRazonSocial().isBlank()) {
                throw new RuntimeException("La razón social es obligatoria para emitir una factura");
            }
        }

        // ─── Cálculo de montos a partir del total del pedido (total = neto + IGV) ───
        BigDecimal montoTotal = pedido.getTotal();
        // neto = total / 1.18
        BigDecimal montoNeto = montoTotal.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoTotal.subtract(montoNeto);

        // ─── Generación simple de serie y correlativo ───
        // NOTA: regla simple acordada (count() + 1). Si más adelante se necesita
        // un correlativo independiente por tipo de comprobante, ajustar aquí.
        String serie = (tipo == TipoComprobante.FACTURA) ? "F001" : "B001";
        long siguienteNumero = comprobanteRepository.count() + 1;
        String correlativo = String.format("%08d", siguienteNumero);

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

        // El pedido pasa a PAID y queda registrado el tipo de comprobante emitido
        pedido.setEstado(EstadoPedido.PAID);
        pedido.setTipoComprobante(tipo);
        pedidoRepository.save(pedido);

        return toDTO(guardado);
    }

    // ─── BUSCAR POR ID ────────────────────────────────────────────────────────
    public ComprobanteResponseDTO buscarPorId(Long id) {
        Comprobante comprobante = comprobanteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con id: " + id));
        return toDTO(comprobante);
    }

    // ─── MAPPER PRIVADO ───────────────────────────────────────────────────────
    private ComprobanteResponseDTO toDTO(Comprobante c) {
        ComprobanteResponseDTO response = new ComprobanteResponseDTO();
        response.setId(c.getId());
        response.setPedidoId(c.getPedido() != null ? c.getPedido().getId() : null);
        response.setTipoComprobante(c.getTipoComprobante() != null ? c.getTipoComprobante().name() : null);
        response.setSerie(c.getSerie());
        // ComprobanteResponseDTO espera Integer; correlativo se guarda como String con ceros a la izquierda
        response.setNumeroCorrelativo(Integer.parseInt(c.getCorrelativo()));
        response.setMetodoPago(c.getMetodo_pago());
        response.setSubtotal(c.getMontoNeto());
        response.setIgv(c.getIgv());
        response.setTotal(c.getMontoTotal());
        response.setRuc(c.getRuc());
        response.setRazonSocial(c.getRazonSocial());
        response.setFechaEmision(c.getFechaEmision());
        return response;
    }
}
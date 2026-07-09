package com.pikudo.service.impl;

import com.pikudo.dto.comprobante.AnularComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.dto.comprobante.NotaCreditoResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.entity.caja.TransaccionPago;
import com.pikudo.exception.BusinessException;
import com.pikudo.mapper.ComprobanteMapper;
import com.pikudo.mapper.NotaCreditoMapper;
import com.pikudo.repository.ComprobanteRepository;
import com.pikudo.repository.NotaCreditoRepository;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.ComprobanteService;
import com.pikudo.service.InventarioService;
import com.pikudo.service.PagoService;
import com.pikudo.service.TicketPrinterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComprobanteMapper comprobanteMapper;
    private final NotaCreditoMapper notaCreditoMapper;
    private final TicketPrinterService ticketPrinterService;
    private final PagoService pagoService;
    private final InventarioService inventarioService;
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    @Override
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new BusinessException("Pedido no encontrado: " + dto.getPedidoId()));
        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new BusinessException("El pedido ya tiene un comprobante emitido");
        }
        TipoComprobante tipo = TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase());
        if (tipo == TipoComprobante.FACTURA && (dto.getRuc() == null || dto.getRazonSocial() == null)) {
            throw new BusinessException("RUC y Razón Social obligatorios para factura");
        }

        BigDecimal montoTotal = pedido.getTotal();
        BigDecimal montoNeto = montoTotal.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoTotal.subtract(montoNeto);
        String serie = (tipo == TipoComprobante.FACTURA) ? "F001" : "B001";

        Comprobante guardado;
        try {
            String correlativo = String.format("%08d", comprobanteRepository.countByTipoComprobante(tipo) + 1);

            Comprobante comprobante = Comprobante.builder()
                    .pedido(pedido)
                    .tipoComprobante(tipo)
                    .serie(serie)
                    .correlativo(correlativo)
                    .montoNeto(montoNeto)
                    .igv(igv)
                    .montoTotal(montoTotal)
                    .ruc(dto.getRuc())
                    .razonSocial(dto.getRazonSocial())
                    .estado(EstadoComprobante.EMITIDO)
                    .build();

            List<TransaccionPago> transacciones = pagoService.procesarPagos(comprobante, dto.getPagos(), montoTotal);
            comprobante.setPagos(transacciones);

            guardado = comprobanteRepository.save(comprobante);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Se produjo una colisión al generar el número de comprobante. Intenta emitir nuevamente.");
        }

        pedido.setEstado(EstadoPedido.PAID);
        pedido.setTipoComprobante(tipo);
        pedidoRepository.save(pedido);

        for (DetallePedido detalle : pedido.getDetalles()) {
            inventarioService.descontarStockPorVenta(detalle.getProducto().getId(), detalle.getCantidad());
        }

        if (tipo == TipoComprobante.FACTURA) {
            ticketPrinterService.imprimirFactura(guardado);
        } else {
            ticketPrinterService.imprimirBoleta(guardado);
        }

        return comprobanteMapper.toDTO(guardado);
    }

    @Override
    @Transactional(readOnly = true)
    public ComprobanteResponseDTO buscarPorId(Long id) {
        Comprobante c = comprobanteRepository.findById(id)
                .orElseThrow(() -> new BusinessException("Comprobante no encontrado con id: " + id));
        return comprobanteMapper.toDTO(c);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComprobanteResponseDTO> listarPorRangoFechas(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new BusinessException("Debe especificar fecha de inicio y fecha de fin");
        }
        if (desde.isAfter(hasta)) {
            throw new BusinessException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        LocalDateTime inicio = desde.atStartOfDay();
        LocalDateTime fin = hasta.atTime(LocalTime.MAX);

        return comprobanteRepository.findByFechaEmisionBetween(inicio, fin).stream()
                .map(comprobanteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public NotaCreditoResponseDTO anular(Long comprobanteId, AnularComprobanteRequestDTO dto) {
        Comprobante comprobante = comprobanteRepository.findById(comprobanteId)
                .orElseThrow(() -> new BusinessException("Comprobante no encontrado con id: " + comprobanteId));

        if (comprobante.getEstado() == EstadoComprobante.ANULADO) {
            throw new BusinessException("Este comprobante ya fue anulado anteriormente");
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        Pedido pedido = comprobante.getPedido();

        // 1. Revertir el stock que se descontó al emitir
        for (DetallePedido detalle : pedido.getDetalles()) {
            inventarioService.revertirStockPorCancelacion(detalle.getProducto().getId(), detalle.getCantidad());
        }

        // 2. El pedido queda cancelado (la venta se deshizo)
        pedido.setEstado(EstadoPedido.CANCELLED);
        pedidoRepository.save(pedido);

        // 3. Generar la Nota de Crédito con correlativo protegido (mismo patrón que el comprobante)
        NotaCredito guardada;
        try {
            String correlativo = String.format("%08d", notaCreditoRepository.count() + 1);

            NotaCredito notaCredito = NotaCredito.builder()
                    .comprobante(comprobante)
                    .correlativo(correlativo)
                    .motivo(dto.getMotivo())
                    .montoDevuelto(comprobante.getMontoTotal())
                    .usuarioEmisor(usuario)
                    .build();

            guardada = notaCreditoRepository.save(notaCredito);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Se produjo una colisión al generar la nota de crédito. Intenta nuevamente.");
        }

        // 4. Marcar el comprobante como ANULADO
        comprobante.setEstado(EstadoComprobante.ANULADO);
        comprobanteRepository.save(comprobante);

        // 5. Imprimir (no debe tumbar la transacción si la impresora falla — ya protegido en TicketPrinterServiceImpl)
        ticketPrinterService.imprimirNotaCredito(guardada);

        log.warn("Comprobante #{} anulado por usuario '{}'. Motivo: {}", comprobanteId, username, dto.getMotivo());

        return notaCreditoMapper.toDTO(guardada);
    }
}
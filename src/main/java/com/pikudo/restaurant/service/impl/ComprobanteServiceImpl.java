package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.dto.comprobante.AnularComprobanteRequestDTO;
import com.pikudo.restaurant.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.restaurant.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.restaurant.dto.comprobante.NotaCreditoResponseDTO;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.EstadoComprobante;
import com.pikudo.restaurant.entity.EstadoPedido;
import com.pikudo.restaurant.entity.EstadoSunat;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.TipoComprobante;
import com.pikudo.restaurant.entity.Usuario;
import com.pikudo.restaurant.entity.caja.TransaccionPago;
import com.pikudo.restaurant.entity.orders.OrderOperationalStatus;
import com.pikudo.restaurant.entity.orders.OrderPayment;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatus;
import com.pikudo.restaurant.entity.orders.OrderPaymentStatusType;
import com.pikudo.restaurant.exception.BusinessException;
import com.pikudo.restaurant.mapper.ComprobanteMapper;
import com.pikudo.restaurant.mapper.NotaCreditoMapper;
import com.pikudo.restaurant.repository.ComprobanteRepository;
import com.pikudo.restaurant.repository.NotaCreditoRepository;
import com.pikudo.restaurant.repository.PedidoRepository;
import com.pikudo.restaurant.repository.UsuarioRepository;
import com.pikudo.restaurant.repository.orders.OrderPaymentRepository;
import com.pikudo.restaurant.service.ComprobanteService;
import com.pikudo.restaurant.service.InventarioService;
import com.pikudo.restaurant.service.TicketPrinterService;
import com.pikudo.restaurant.service.orders.TableSessionService;
import com.pikudo.restaurant.service.sunat.DocumentSequenceService;
import com.pikudo.restaurant.service.sunat.SunatSubmissionJobService;
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

    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");

    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final UsuarioRepository usuarioRepository;
    private final OrderPaymentRepository orderPaymentRepository;
    private final ComprobanteMapper comprobanteMapper;
    private final NotaCreditoMapper notaCreditoMapper;
    private final TicketPrinterService ticketPrinterService;
    private final InventarioService inventarioService;
    private final TableSessionService tableSessionService;
    private final DocumentSequenceService documentSequenceService;
    private final SunatSubmissionJobService sunatSubmissionJobService;

    @Override
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new BusinessException("Pedido no encontrado: " + dto.getPedidoId()));
        if (comprobanteRepository.existsByPedidoId(pedido.getId())) {
            throw new BusinessException("El pedido ya tiene un comprobante emitido");
        }
        if (pedido.getEstadoPago() != OrderPaymentStatus.PAID) {
            throw new BusinessException("No se puede emitir comprobante: el pedido aun tiene saldo pendiente.");
        }

        Usuario cajero = currentUser();
        TipoComprobante tipo = normalizeTipo(dto);
        validateCliente(dto, tipo);

        BigDecimal montoTotal = pedido.getTotal();
        BigDecimal montoNeto = montoTotal.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoTotal.subtract(montoNeto);
        String serie = serieFor(tipo);

        Comprobante guardado;
        try {
            String correlativo = documentSequenceService.nextCorrelativo(sequenceType(tipo), serie);
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
                    .tipoDocumentoCliente(resolveTipoDocumento(dto, tipo))
                    .numeroDocumentoCliente(resolveNumeroDocumento(dto, tipo))
                    .direccionCliente(dto.getDireccionCliente())
                    .clienteNombreSnapshot(resolveClienteSnapshot(dto, tipo))
                    .documentFolderType(folderTypeFor(tipo))
                    .estado(EstadoComprobante.EMITIDO)
                    .estadoSunat(EstadoSunat.NO_ENVIADO)
                    .build();
            comprobante.setPagos(buildTransaccionesFromOrderPayments(comprobante, pedido));
            guardado = comprobanteRepository.save(comprobante);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Se produjo una colision al generar el numero de comprobante. Intenta emitir nuevamente.");
        }

        pedido.setCajero(cajero);
        pedido.setEstado(EstadoPedido.PAID);
        pedido.setEstadoPago(OrderPaymentStatus.PAID);
        if (pedido.getEstadoOperativo() != OrderOperationalStatus.CANCELLED
                && pedido.getEstadoOperativo() != OrderOperationalStatus.REJECTED) {
            pedido.setEstadoOperativo(OrderOperationalStatus.DELIVERED);
        }
        pedido.setTipoComprobante(tipo);
        pedidoRepository.save(pedido);
        tableSessionService.closeIfNoOpenOrders(pedido.getTableSession(), cajero);

        for (DetallePedido detalle : pedido.getDetalles()) {
            inventarioService.descontarStockPorVenta(detalle.getProducto().getId(), detalle.getCantidad());
        }

        sunatSubmissionJobService.enqueue(guardado);

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

        Usuario usuario = currentUser();
        Pedido pedido = comprobante.getPedido();

        for (DetallePedido detalle : pedido.getDetalles()) {
            inventarioService.revertirStockPorCancelacion(detalle.getProducto().getId(), detalle.getCantidad());
        }

        pedido.setEstado(EstadoPedido.CANCELLED);
        pedido.setEstadoOperativo(OrderOperationalStatus.CANCELLED);
        pedido.setEstadoPago(OrderPaymentStatus.VOIDED);
        pedidoRepository.save(pedido);
        tableSessionService.closeIfNoOpenOrders(pedido.getTableSession(), usuario);

        NotaCredito guardada;
        try {
            String correlativo = documentSequenceService.nextCorrelativo(TipoComprobante.NOTA_CREDITO, "NC01");
            NotaCredito notaCredito = NotaCredito.builder()
                    .comprobante(comprobante)
                    .serie("NC01")
                    .correlativo(correlativo)
                    .motivo(dto.getMotivo())
                    .montoDevuelto(comprobante.getMontoTotal())
                    .usuarioEmisor(usuario)
                    .estadoSunat(EstadoSunat.NO_ENVIADO)
                    .build();
            guardada = notaCreditoRepository.save(notaCredito);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException("Se produjo una colision al generar la nota de credito. Intenta nuevamente.");
        }

        comprobante.setEstado(EstadoComprobante.ANULADO);
        comprobanteRepository.save(comprobante);
        sunatSubmissionJobService.enqueue(guardada);

        ticketPrinterService.imprimirNotaCredito(guardada);
        log.warn("Comprobante #{} anulado por usuario '{}'. Motivo: {}", comprobanteId, usuario.getUsername(), dto.getMotivo());
        return notaCreditoMapper.toDTO(guardada);
    }

    private List<TransaccionPago> buildTransaccionesFromOrderPayments(Comprobante comprobante, Pedido pedido) {
        List<OrderPayment> confirmedPayments = orderPaymentRepository.findByPedidoIdOrderByFechaCreacionAscIdAsc(pedido.getId())
                .stream()
                .filter(payment -> payment.getStatus() == OrderPaymentStatusType.CONFIRMED)
                .toList();
        if (confirmedPayments.isEmpty()) {
            throw new BusinessException("El pedido no tiene pagos operativos confirmados para emitir comprobante.");
        }
        return confirmedPayments.stream()
                .map(payment -> TransaccionPago.builder()
                        .comprobante(comprobante)
                        .metodoPago(payment.getMetodoPago())
                        .monto(payment.getMonto())
                        .build())
                .toList();
    }

    private TipoComprobante normalizeTipo(ComprobanteRequestDTO dto) {
        TipoComprobante requested = TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase());
        if (requested == TipoComprobante.BOLETA) {
            boolean hasDocument = dto.getNumeroDocumentoCliente() != null && !dto.getNumeroDocumentoCliente().isBlank();
            return hasDocument ? TipoComprobante.BOLETA_CON_DOCUMENTO : TipoComprobante.BOLETA_SIMPLE;
        }
        return requested;
    }

    private void validateCliente(ComprobanteRequestDTO dto, TipoComprobante tipo) {
        if (tipo == TipoComprobante.FACTURA && (isBlank(dto.getRuc()) || isBlank(dto.getRazonSocial()))) {
            throw new BusinessException("RUC y razon social son obligatorios para factura");
        }
        if (tipo == TipoComprobante.BOLETA_CON_DOCUMENTO
                && (isBlank(dto.getTipoDocumentoCliente()) || isBlank(dto.getNumeroDocumentoCliente()))) {
            throw new BusinessException("Tipo y numero de documento son obligatorios para boleta con documento");
        }
    }

    private TipoComprobante sequenceType(TipoComprobante tipo) {
        return tipo == TipoComprobante.BOLETA_CON_DOCUMENTO ? TipoComprobante.BOLETA_SIMPLE : tipo;
    }

    private String serieFor(TipoComprobante tipo) {
        return switch (tipo) {
            case FACTURA -> "F001";
            case NOTA_CREDITO -> "NC01";
            case NOTA_DEBITO -> "ND01";
            default -> "B001";
        };
    }

    private String folderTypeFor(TipoComprobante tipo) {
        return switch (tipo) {
            case FACTURA -> "FACTURAS";
            case NOTA_CREDITO -> "NOTAS_DE_CREDITO";
            case NOTA_DEBITO -> "NOTAS_DE_DEBITO";
            default -> "BOLETAS";
        };
    }

    private String resolveTipoDocumento(ComprobanteRequestDTO dto, TipoComprobante tipo) {
        return tipo == TipoComprobante.BOLETA_SIMPLE ? "SIN_DOCUMENTO" : dto.getTipoDocumentoCliente();
    }

    private String resolveNumeroDocumento(ComprobanteRequestDTO dto, TipoComprobante tipo) {
        return tipo == TipoComprobante.BOLETA_SIMPLE ? null : dto.getNumeroDocumentoCliente();
    }

    private String resolveClienteSnapshot(ComprobanteRequestDTO dto, TipoComprobante tipo) {
        if (tipo == TipoComprobante.FACTURA) {
            return dto.getRazonSocial();
        }
        return tipo == TipoComprobante.BOLETA_SIMPLE ? "PUBLICO_GENERAL" : dto.getNumeroDocumentoCliente();
    }

    private Usuario currentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}

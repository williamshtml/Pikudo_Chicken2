package com.pikudo.service.impl;
import com.pikudo.mapper.ComprobanteMapper; // Inyectado
import com.pikudo.dto.comprobante.ComprobanteRequestDTO;
import com.pikudo.dto.comprobante.ComprobanteResponseDTO;
import com.pikudo.entity.*;
import com.pikudo.repository.ComprobanteRepository;
import com.pikudo.repository.PedidoRepository;
import com.pikudo.service.ComprobanteService;
import com.pikudo.service.PagoService;
import com.pikudo.service.TicketPrinterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
@Service
@RequiredArgsConstructor
@Transactional
public class ComprobanteServiceImpl implements ComprobanteService {
    private final ComprobanteRepository comprobanteRepository;
    private final PedidoRepository pedidoRepository;
    private final ComprobanteMapper comprobanteMapper; // Inyectado
    private final TicketPrinterService ticketPrinterService; // Para imprimir boleta/factura
    private final PagoService pagoService; // Nuevo: valida y asigna el metodo de pago
    private static final BigDecimal TASA_IGV = new BigDecimal("0.18");
    @Override
    public ComprobanteResponseDTO emitir(ComprobanteRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.getPedidoId())
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + dto.getPedidoId()));
        if (pedido.getEstado() == EstadoPedido.PAID) {
            throw new RuntimeException("El pedido ya tiene un comprobante emitido");
        }
        TipoComprobante tipo = TipoComprobante.valueOf(dto.getTipoComprobante().toUpperCase());
        if (tipo == TipoComprobante.FACTURA && (dto.getRuc() == null || dto.getRazonSocial() == null)) {
            throw new RuntimeException("RUC y Razón Social obligatorios para factura");
        }

        // Valida el metodo de pago contra el catalogo (MetodoPago) y lo asigna al pedido.
        // Esto es lo que permite que el cuadre de caja (calcularTotalVentasPorMetodoTipo)
        // pueda agrupar correctamente por tipo (EFECTIVO/TARJETA/DIGITAL).
        pagoService.aplicarMetodoPago(pedido, dto.getMetodoPago());

        BigDecimal montoTotal = pedido.getTotal();
        BigDecimal montoNeto = montoTotal.divide(BigDecimal.ONE.add(TASA_IGV), 2, RoundingMode.HALF_UP);
        BigDecimal igv = montoTotal.subtract(montoNeto);
        String serie = (tipo == TipoComprobante.FACTURA) ? "F001" : "B001";
        String correlativo = String.format("%08d", comprobanteRepository.countByTipoComprobante(tipo) + 1);
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

        // Imprime el ticket termico de boleta o factura, segun corresponda.
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
                .orElseThrow(() -> new RuntimeException("Comprobante no encontrado con id: " + id));
        return comprobanteMapper.toDTO(c);
    }
}
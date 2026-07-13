package com.pikudo.restaurant.service.impl;

import com.pikudo.restaurant.entity.AreaPreparacion;
import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.Impresora;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.caja.Caja;
import com.pikudo.restaurant.repository.ImpresoraRepository;
import com.pikudo.restaurant.service.TicketPrinterService;
import com.pikudo.restaurant.service.impresion.MotorImpresion;
import com.pikudo.restaurant.service.impresion.formatos.FormatoBoleta;
import com.pikudo.restaurant.service.impresion.formatos.FormatoComanda;
import com.pikudo.restaurant.service.impresion.formatos.FormatoFactura;
import com.pikudo.restaurant.service.impresion.formatos.FormatoNotaCredito;
import com.pikudo.restaurant.service.impresion.formatos.FormatoPrecuenta;
import com.pikudo.restaurant.service.impresion.formatos.FormatoReporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketPrinterServiceImpl implements TicketPrinterService {

    private final ImpresoraRepository impresoraRepository;
    private final MotorImpresion motorImpresion;
    private final FormatoComanda formatoComanda;
    private final FormatoPrecuenta formatoPrecuenta;
    private final FormatoBoleta formatoBoleta;
    private final FormatoFactura formatoFactura;
    private final FormatoReporte formatoReporte;
    private final FormatoNotaCredito formatoNotaCredito;

    @Override
    public void imprimirTicketsPorArea(Pedido pedido) {
        Map<AreaPreparacion, List<DetallePedido>> porArea = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getCategoria().getAreaPreparacion() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getCategoria().getAreaPreparacion()
                ));

        porArea.forEach((area, detalles) -> {
            impresoraRepository.findByAreaAndActivaTrue(area).ifPresentOrElse(
                    impresora -> imprimirSeguro(() -> {
                        byte[] contenido = formatoComanda.construir(pedido, area, detalles);
                        motorImpresion.enviarComandos(impresora, contenido);
                    }, "comanda del área " + area + " (pedido #" + pedido.getId() + ")"),
                    () -> log.warn("No hay impresora activa configurada para el area {}", area)
            );
        });
    }

    @Override
    public void imprimirPrecuentaDelivery(Pedido pedido, Impresora impresoraCaja) {
        imprimirSeguro(() -> {
            byte[] contenido = formatoPrecuenta.construir(pedido);
            motorImpresion.enviarComandos(impresoraCaja, contenido);
        }, "precuenta delivery (pedido #" + pedido.getId() + ")");
    }

    @Override
    public void imprimirBoleta(Comprobante comprobante) {
        imprimirEnCaja(formatoBoleta.construir(comprobante), "boleta (comprobante #" + comprobante.getId() + ")");
    }

    @Override
    public void imprimirFactura(Comprobante comprobante) {
        imprimirEnCaja(formatoFactura.construir(comprobante), "factura (comprobante #" + comprobante.getId() + ")");
    }

    @Override
    public void imprimirReporteCierreCaja(Caja caja) {
        imprimirEnCaja(formatoReporte.construir(caja), "reporte de cierre (caja #" + caja.getId() + ")");
    }

    @Override
    public void imprimirNotaCredito(NotaCredito notaCredito) {
        imprimirEnCaja(formatoNotaCredito.construir(notaCredito), "nota de crédito #" + notaCredito.getId());
    }

    private void imprimirEnCaja(byte[] contenido, String descripcion) {
        impresoraRepository.findByAreaAndActivaTrue(AreaPreparacion.CAJA).ifPresentOrElse(
                impresora -> imprimirSeguro(() -> motorImpresion.enviarComandos(impresora, contenido), descripcion),
                () -> log.warn("No hay impresora activa configurada para CAJA ({})", descripcion)
        );
    }

    /**
     * Ejecuta una operación de impresión sin dejar que un fallo de hardware
     * (impresora apagada, sin papel, desconectada) tumbe la transacción de
     * negocio que la originó (venta ya cobrada, pedido ya creado, etc.).
     * El fallo se registra en logs para que se pueda reimprimir manualmente,
     * pero NUNCA debe revertir dinero ya cobrado o un pedido ya comprometido.
     */
    private void imprimirSeguro(Runnable accionImpresion, String descripcion) {
        try {
            accionImpresion.run();
        } catch (Exception e) {
            log.error("Fallo al imprimir {}: {}. La operación de negocio continúa; " +
                    "reimprimir manualmente cuando la impresora esté disponible.",
                    descripcion, e.getMessage(), e);
        }
    }
}
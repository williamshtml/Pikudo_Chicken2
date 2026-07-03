package com.pikudo.service.impl;

import com.pikudo.entity.AreaPreparacion;
import com.pikudo.entity.Comprobante;
import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.Impresora;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.caja.Caja;
import com.pikudo.repository.ImpresoraRepository;
import com.pikudo.service.TicketPrinterService;
import com.pikudo.service.impresion.MotorImpresion;
import com.pikudo.service.impresion.formatos.FormatoBoleta;
import com.pikudo.service.impresion.formatos.FormatoComanda;
import com.pikudo.service.impresion.formatos.FormatoFactura;
import com.pikudo.service.impresion.formatos.FormatoPrecuenta;
import com.pikudo.service.impresion.formatos.FormatoReporte;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Orquesta QUE se imprime y DONDE, delegando:
 * - el contenido (formato del documento) a las clases Formato*
 * - el transporte (hablar con la impresora fisica) a MotorImpresion
 */
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

    @Override
    public void imprimirTicketsPorArea(Pedido pedido) {
        Map<AreaPreparacion, List<DetallePedido>> porArea = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getCategoria().getAreaPreparacion() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getCategoria().getAreaPreparacion()
                ));

        porArea.forEach((area, detalles) -> {
            impresoraRepository.findByAreaAndActivaTrue(area).ifPresentOrElse(
                    impresora -> {
                        byte[] contenido = formatoComanda.construir(pedido, area, detalles);
                        motorImpresion.enviarComandos(impresora, contenido);
                    },
                    () -> log.warn("No hay impresora activa configurada para el area {}", area)
            );
        });
    }

    @Override
    public void imprimirPrecuentaDelivery(Pedido pedido, Impresora impresoraCaja) {
        byte[] contenido = formatoPrecuenta.construir(pedido);
        motorImpresion.enviarComandos(impresoraCaja, contenido);
    }

    @Override
    public void imprimirBoleta(Comprobante comprobante) {
        imprimirEnCaja(formatoBoleta.construir(comprobante));
    }

    @Override
    public void imprimirFactura(Comprobante comprobante) {
        imprimirEnCaja(formatoFactura.construir(comprobante));
    }

    @Override
    public void imprimirReporteCierreCaja(Caja caja) {
        imprimirEnCaja(formatoReporte.construir(caja));
    }

    private void imprimirEnCaja(byte[] contenido) {
        impresoraRepository.findByAreaAndActivaTrue(AreaPreparacion.CAJA).ifPresentOrElse(
                impresora -> motorImpresion.enviarComandos(impresora, contenido),
                () -> log.warn("No hay impresora activa configurada para CAJA")
        );
    }
}
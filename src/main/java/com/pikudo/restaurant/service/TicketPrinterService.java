package com.pikudo.restaurant.service;

import com.pikudo.restaurant.entity.Comprobante;
import com.pikudo.restaurant.entity.Impresora;
import com.pikudo.restaurant.entity.NotaCredito;
import com.pikudo.restaurant.entity.Pedido;
import com.pikudo.restaurant.entity.caja.Caja;

public interface TicketPrinterService {
    void imprimirTicketsPorArea(Pedido pedido);
    void imprimirPrecuentaDelivery(Pedido pedido, Impresora impresoraCaja);
    void imprimirBoleta(Comprobante comprobante);
    void imprimirFactura(Comprobante comprobante);
    void imprimirReporteCierreCaja(Caja caja);
    void imprimirNotaCredito(NotaCredito notaCredito);
}
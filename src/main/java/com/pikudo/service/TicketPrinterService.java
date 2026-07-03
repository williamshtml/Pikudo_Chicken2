package com.pikudo.service;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.Impresora;
import com.pikudo.entity.Pedido;
import com.pikudo.entity.caja.Caja;

public interface TicketPrinterService {
    void imprimirTicketsPorArea(Pedido pedido);
    void imprimirPrecuentaDelivery(Pedido pedido, Impresora impresoraCaja);
    void imprimirBoleta(Comprobante comprobante);
    void imprimirFactura(Comprobante comprobante);
    void imprimirReporteCierreCaja(Caja caja);
}
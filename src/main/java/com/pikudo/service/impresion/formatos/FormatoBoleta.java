package com.pikudo.service.impresion.formatos;

import com.pikudo.entity.Comprobante;
import com.pikudo.entity.DetallePedido;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Arma el contenido de la boleta de venta (formato ticket termico 80mm).
 *
 * NOTA: Actualmente Comprobante no tiene datos de firma digital/QR de SUNAT
 * (hash, codigo CPE, etc.), porque aun no hay integracion con un OSE/PSE
 * (ej. Nubefact, Efact). Cuando se integre, el QR se agrega aqui mismo,
 * generalmente como datos raster (comando ESC/POS "GS v 0") justo antes del corte.
 */
@Component
public class FormatoBoleta {

    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] DOUBLE_SIZE = {0x1D, 0x21, 0x11};
    private static final byte[] NORMAL_SIZE = {0x1D, 0x21, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};

    public byte[] construir(Comprobante comprobante) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(INIT);
            out.write(CENTER);
            out.write(BOLD_ON);
            escribir(out, "PIKUDO CHICKEN\n");
            out.write(NORMAL_SIZE);
            out.write(BOLD_OFF);
            escribir(out, "RUC: [Pendiente configurar en ConfiguracionEmpresa]\n");
            escribir(out, "\n");

            out.write(DOUBLE_SIZE);
            out.write(BOLD_ON);
            escribir(out, "BOLETA DE VENTA ELECTRONICA\n");
            out.write(NORMAL_SIZE);
            escribir(out, comprobante.getSerie() + "-" + comprobante.getCorrelativo() + "\n");
            out.write(BOLD_OFF);
            escribir(out, "\n");

            out.write(LEFT);
            escribir(out, "Fecha: " + comprobante.getFechaEmision() + "\n");
            escribir(out, "Pedido: #" + comprobante.getPedido().getId() + "\n");
            escribir(out, "--------------------------------\n");

            for (DetallePedido d : comprobante.getPedido().getDetalles()) {
                String linea = String.format("%.2f %-20s %6.2f\n",
                        d.getCantidad().doubleValue(),
                        d.getProducto().getNombre(),
                        d.getSubtotal());
                escribir(out, linea);
            }

            escribir(out, "--------------------------------\n");
            escribir(out, "Op. Gravada  : S/ " + comprobante.getMontoNeto() + "\n");
            escribir(out, "IGV (18%)    : S/ " + comprobante.getIgv() + "\n");
            out.write(BOLD_ON);
            escribir(out, "TOTAL        : S/ " + comprobante.getMontoTotal() + "\n");
            out.write(BOLD_OFF);

            escribir(out, "\n");
            escribir(out, "Forma de pago:\n");
            for (var pago : comprobante.getPagos()) {
                escribir(out, "  " + pago.getMetodoPago().getNombre() + " : S/ " + pago.getMonto() + "\n");
            }

            // TODO: cuando se integre un OSE/PSE, imprimir aqui el codigo QR
            // (comando raster GS v 0) generado a partir del hash/CPE que devuelva el proveedor.

            escribir(out, "\n");
            out.write(CENTER);
            escribir(out, "Gracias por su compra\n\n\n");
            out.write(CUT);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir boleta", e);
        }
        return out.toByteArray();
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}
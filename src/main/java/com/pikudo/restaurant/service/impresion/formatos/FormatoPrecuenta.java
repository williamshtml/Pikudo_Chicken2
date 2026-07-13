package com.pikudo.restaurant.service.impresion.formatos;

import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.Pedido;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Arma el contenido de la precuenta que se entrega al motorizado.
 * NO es un comprobante de pago valido ante SUNAT.
 */
@Component
public class FormatoPrecuenta {

    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};

    public byte[] construir(Pedido pedido) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(INIT);
            out.write(CENTER);
            out.write(BOLD_ON);
            escribir(out, "PIKUDO CHICKEN\n");
            out.write(BOLD_OFF);
            out.write(LEFT);
            escribir(out, "\n");

            escribir(out, "Pedido        : " + pedido.getId() + "\n");
            escribir(out, "Fecha         : " + pedido.getFechaCreacion() + "\n");

            if (pedido.getCajero() != null) {
                escribir(out, "Caja          : " + pedido.getCajero().getUsername() + "\n");
            }
            if (pedido.getRepartidor() != null) {
                escribir(out, "Repartidor    : " + pedido.getRepartidor().getUsername() + "\n");
            }

            escribir(out, "\n");
            escribir(out, "Telefono      : " + valorOGuion(pedido.getTelefonoCliente()) + "\n");
            escribir(out, "Direccion     : " + valorOGuion(pedido.getDireccion()) + "\n");
            if (pedido.getObservacionesPedido() != null && !pedido.getObservacionesPedido().isBlank()) {
                escribir(out, "Observacion   : " + pedido.getObservacionesPedido() + "\n");
            }

            escribir(out, "\n--------------------------------\n");
            escribir(out, "Cant. Producto            Subtotal\n");
            escribir(out, "--------------------------------\n");

            for (DetallePedido d : pedido.getDetalles()) {
                String linea = String.format("%.2f %-20s %6.2f\n",
                        d.getCantidad().doubleValue(),
                        d.getProducto().getNombre(),
                        d.getSubtotal());
                escribir(out, linea);
            }

            escribir(out, "--------------------------------\n");
            out.write(BOLD_ON);
            escribir(out, "Total S/      : " + pedido.getTotal() + "\n");
            out.write(BOLD_OFF);

            escribir(out, "\n");
            out.write(CENTER);
            escribir(out, "ESTE DOCUMENTO NO ES UN\n");
            escribir(out, "COMPROBANTE DE PAGO,\n");
            escribir(out, "ES UN PRELIMINAR DE SU CUENTA\n\n\n");
            out.write(CUT);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir precuenta de delivery", e);
        }
        return out.toByteArray();
    }

    private String valorOGuion(String valor) {
        return (valor == null || valor.isBlank()) ? "-" : valor;
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}
package com.pikudo.restaurant.service.impresion.formatos;

import com.pikudo.restaurant.entity.AreaPreparacion;
import com.pikudo.restaurant.entity.DetallePedido;
import com.pikudo.restaurant.entity.Pedido;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Arma el contenido (bytes ESC/POS) del ticket de comanda que va a
 * cocina, bar u horno. Solo sabe de "que texto va", no de sockets ni IPs.
 */
@Component
public class FormatoComanda {

    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] DOUBLE_SIZE = {0x1D, 0x21, 0x11};
    private static final byte[] NORMAL_SIZE = {0x1D, 0x21, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};

    public byte[] construir(Pedido pedido, AreaPreparacion area, List<DetallePedido> detalles) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(INIT);
            out.write(CENTER);
            out.write(DOUBLE_SIZE);
            out.write(BOLD_ON);
            escribir(out, area.name() + "\n");
            out.write(NORMAL_SIZE);
            out.write(BOLD_OFF);
            escribir(out, "\n");

            out.write(LEFT);
            escribir(out, "Pedido #" + pedido.getId() + "\n");

            String origenInfo = pedido.getMesa() != null
                    ? "Mesa: " + pedido.getMesa().getNumero()
                    : "DELIVERY";
            escribir(out, origenInfo + "\n");
            escribir(out, "Hora: " + pedido.getFechaCreacion() + "\n");
            escribir(out, "--------------------------------\n");

            for (DetallePedido d : detalles) {
                escribir(out, d.getCantidad() + "x  " + d.getProducto().getNombre() + "\n");
                if (d.getObservaciones() != null && !d.getObservaciones().isBlank()) {
                    escribir(out, "   * " + d.getObservaciones() + "\n");
                }
            }

            escribir(out, "--------------------------------\n\n\n");
            out.write(CUT);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir ticket de comanda", e);
        }
        return out.toByteArray();
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}
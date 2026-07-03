package com.pikudo.service.impresion.formatos;

import com.pikudo.entity.caja.Caja;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

/**
 * Arma el contenido del ticket de cierre de caja (reporte de turno).
 * Se imprime en la misma impresora de CAJA, junto con boletas/facturas/precuentas.
 */
@Component
public class FormatoReporte {

    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] DOUBLE_SIZE = {0x1D, 0x21, 0x11};
    private static final byte[] NORMAL_SIZE = {0x1D, 0x21, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};

    public byte[] construir(Caja caja) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            out.write(INIT);
            out.write(CENTER);
            out.write(DOUBLE_SIZE);
            out.write(BOLD_ON);
            escribir(out, "CIERRE DE CAJA\n");
            out.write(NORMAL_SIZE);
            out.write(BOLD_OFF);
            escribir(out, "\n");

            out.write(LEFT);
            escribir(out, "Caja          : " + caja.getNumeroCaja() + "\n");
            escribir(out, "Cajero        : " + caja.getUsuario().getUsername() + "\n");
            escribir(out, "Apertura      : " + caja.getFechaApertura() + "\n");
            escribir(out, "Cierre        : " + caja.getFechaCierre() + "\n");
            escribir(out, "--------------------------------\n");

            escribir(out, "Monto inicial : S/ " + caja.getMontoInicial() + "\n");
            escribir(out, "\n");
            escribir(out, "VENTAS POR METODO DE PAGO\n");
            escribir(out, "Efectivo      : S/ " + caja.getMontoVentasEfectivo() + "\n");
            escribir(out, "Tarjeta       : S/ " + caja.getMontoVentasTarjeta() + "\n");
            escribir(out, "Yape/Plin     : S/ " + caja.getMontoVentasDigital() + "\n");

            BigDecimal totalVentas = sumar(caja.getMontoVentasEfectivo(), caja.getMontoVentasTarjeta(), caja.getMontoVentasDigital());
            out.write(BOLD_ON);
            escribir(out, "Total ventas  : S/ " + totalVentas + "\n");
            out.write(BOLD_OFF);

            escribir(out, "\n");
            escribir(out, "Gastos        : S/ " + caja.getMontoGastos() + "\n");
            escribir(out, "--------------------------------\n");

            escribir(out, "Final sistema : S/ " + valorOCero(caja.getMontoFinalSistema()) + "\n");
            escribir(out, "Final real    : S/ " + valorOCero(caja.getMontoFinalReal()) + "\n");

            BigDecimal diferencia = diferencia(caja.getMontoFinalSistema(), caja.getMontoFinalReal());
            out.write(BOLD_ON);
            escribir(out, "Diferencia    : S/ " + diferencia + "\n");
            out.write(BOLD_OFF);

            if (caja.getObservaciones() != null && !caja.getObservaciones().isBlank()) {
                escribir(out, "\n");
                escribir(out, "Obs: " + caja.getObservaciones() + "\n");
            }

            escribir(out, "\n\n");
            out.write(CUT);
        } catch (IOException e) {
            throw new RuntimeException("Error al construir reporte de cierre de caja", e);
        }
        return out.toByteArray();
    }

    private BigDecimal sumar(BigDecimal... montos) {
        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal m : montos) {
            if (m != null) total = total.add(m);
        }
        return total;
    }

    private BigDecimal diferencia(BigDecimal sistema, BigDecimal real) {
        if (sistema == null || real == null) return BigDecimal.ZERO;
        return real.subtract(sistema);
    }

    private BigDecimal valorOCero(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }

    private void escribir(ByteArrayOutputStream out, String texto) throws IOException {
        out.write(texto.getBytes(StandardCharsets.UTF_8));
    }
}
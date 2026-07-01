package com.pikudo.service.impl;

import com.pikudo.entity.AreaPreparacion;
import com.pikudo.entity.DetallePedido;
import com.pikudo.entity.Impresora;
import com.pikudo.entity.Pedido;
import com.pikudo.repository.ImpresoraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketPrinterService {

    private final ImpresoraRepository impresoraRepository;

    // Comandos ESC/POS basicos
    private static final byte[] INIT = {0x1B, 0x40};
    private static final byte[] BOLD_ON = {0x1B, 0x45, 0x01};
    private static final byte[] BOLD_OFF = {0x1B, 0x45, 0x00};
    private static final byte[] CENTER = {0x1B, 0x61, 0x01};
    private static final byte[] LEFT = {0x1B, 0x61, 0x00};
    private static final byte[] DOUBLE_SIZE = {0x1D, 0x21, 0x11};
    private static final byte[] NORMAL_SIZE = {0x1D, 0x21, 0x00};
    private static final byte[] CUT = {0x1D, 0x56, 0x41, 0x00};
    private static final byte[] LF = "\n".getBytes(StandardCharsets.UTF_8);

    public void imprimirTicketsPorArea(Pedido pedido) {
        Map<AreaPreparacion, List<DetallePedido>> porArea = pedido.getDetalles().stream()
                .filter(d -> d.getProducto().getCategoria().getAreaPreparacion() != null)
                .collect(Collectors.groupingBy(
                        d -> d.getProducto().getCategoria().getAreaPreparacion()
                ));

        porArea.forEach((area, detalles) -> {
            Impresora impresora = impresoraRepository.findByAreaAndActivaTrue(area)
                    .orElse(null);

            if (impresora == null) {
                log.warn("No hay impresora activa configurada para el area {}", area);
                return; // No rompe el flujo del pedido si falta una impresora
            }

            imprimir(impresora, pedido, area, detalles);
        });
    }

    private void imprimir(Impresora impresora, Pedido pedido, AreaPreparacion area, List<DetallePedido> detalles) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(impresora.getIp(), impresora.getPuerto()), 3000);
            OutputStream out = socket.getOutputStream();

            out.write(INIT);
            out.write(CENTER);
            out.write(DOUBLE_SIZE);
            out.write(BOLD_ON);
            out.write((area.name() + "\n").getBytes(StandardCharsets.UTF_8));
            out.write(NORMAL_SIZE);
            out.write(BOLD_OFF);
            out.write(LF);

            out.write(LEFT);
            out.write(("Pedido #" + pedido.getId() + "\n").getBytes(StandardCharsets.UTF_8));

            String origenInfo = pedido.getMesa() != null
                    ? "Mesa: " + pedido.getMesa().getNumero()
                    : "DELIVERY";
            out.write((origenInfo + "\n").getBytes(StandardCharsets.UTF_8));

            out.write(("Hora: " + pedido.getFechaCreacion() + "\n").getBytes(StandardCharsets.UTF_8));
            out.write("--------------------------------\n".getBytes(StandardCharsets.UTF_8));

            for (DetallePedido d : detalles) {
                String linea = d.getCantidad() + "x  " + d.getProducto().getNombre() + "\n";
                out.write(linea.getBytes(StandardCharsets.UTF_8));

                if (d.getObservaciones() != null && !d.getObservaciones().isBlank()) {
                    out.write(("   * " + d.getObservaciones() + "\n").getBytes(StandardCharsets.UTF_8));
                }
            }

            out.write("--------------------------------\n".getBytes(StandardCharsets.UTF_8));
            out.write(LF);
            out.write(LF);
            out.write(CUT);

            out.flush();
            log.info("Ticket impreso correctamente en {} ({}:{})", area, impresora.getIp(), impresora.getPuerto());

        } catch (Exception e) {
            log.error("Error al imprimir ticket en {} ({}:{}): {}", area, impresora.getIp(), impresora.getPuerto(), e.getMessage());
            // No se relanza la excepcion para no bloquear la creacion del pedido
            // si una impresora esta apagada o desconectada
        }
    }
}
package com.pikudo.service.impresion.impl;

import com.pikudo.entity.Impresora;
import com.pikudo.service.impresion.MotorImpresion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

@Service
@Slf4j
public class MotorImpresionImpl implements MotorImpresion {

    @Override
    public void enviarComandos(Impresora impresora, byte[] comandos) {
        if (impresora == null || !impresora.isActiva()) {
            log.warn("Impresora no disponible o inactiva");
            return;
        }
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(impresora.getIp(), impresora.getPuerto()), 3000);
            try (OutputStream out = socket.getOutputStream()) {
                out.write(comandos);
                out.flush();
            }
            log.info("Impresión enviada correctamente a {}:{}", impresora.getIp(), impresora.getPuerto());
        } catch (Exception e) {
            log.error("Error al conectar con impresora {}: {}", impresora.getIp(), e.getMessage());
        }
    }
}
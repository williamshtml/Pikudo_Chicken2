package com.pikudo.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mantiene en memoria quienes (repartidores) tienen una sesion WebSocket activa
 * en este momento. No se persiste en base de datos a proposito: es informacion
 * de "ahora mismo", no un historial.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenciaRepartidorServiceImpl {

    private final SimpMessagingTemplate messagingTemplate;

    // sessionId (de WebSocket) -> repartidorId
    private final Map<String, Long> sesionesActivas = new ConcurrentHashMap<>();

    public void registrarConexion(String sessionId, Long repartidorId) {
        sesionesActivas.put(sessionId, repartidorId);
        log.info("Repartidor {} conectado (sesion {})", repartidorId, sessionId);
        broadcastEstado();
    }

    @EventListener
    public void manejarDesconexion(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        Long repartidorId = sesionesActivas.remove(sessionId);

        if (repartidorId != null) {
            log.info("Repartidor {} desconectado (sesion {})", repartidorId, sessionId);
            broadcastEstado();
        }
    }

    private void broadcastEstado() {
        messagingTemplate.convertAndSend("/topic/repartidores/estado", idsConectados());
    }

    public List<Long> idsConectados() {
        return sesionesActivas.values().stream().distinct().toList();
    }

    public boolean estaConectado(Long repartidorId) {
        return sesionesActivas.containsValue(repartidorId);
    }
}
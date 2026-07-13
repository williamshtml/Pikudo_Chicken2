package com.pikudo.restaurant.service.impl;
import com.pikudo.restaurant.dto.tracking.UbicacionDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * Mantiene en memoria quienes (repartidores) tienen una sesion WebSocket activa
 * en este momento, y su ultima posicion GPS conocida. No se persiste en base
 * de datos a proposito: es informacion de "ahora mismo", no un historial.
 *
 * NOTA DE ESCALABILIDAD: al vivir en memoria (ConcurrentHashMap), este estado
 * es local a esta unica instancia del backend. Si en el futuro se escala a
 * multiples instancias detras de un load balancer, esto debe migrarse a Redis
 * (o similar) para que todas las instancias compartan el mismo estado.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PresenciaRepartidorServiceImpl {
    private final SimpMessagingTemplate messagingTemplate;

    // sessionId (de WebSocket) -> repartidorId
    private final Map<String, Long> sesionesActivas = new ConcurrentHashMap<>();

    // repartidorId -> ultima ubicacion GPS conocida (para carga inicial del panel admin)
    private final Map<Long, UbicacionDTO> ultimasUbicaciones = new ConcurrentHashMap<>();

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
            // Se retira tambien su ultima ubicacion: si esta desconectado, el
            // panel admin no deberia seguir mostrandolo como activo en el mapa.
            ultimasUbicaciones.remove(repartidorId);
            broadcastEstado();
        }
    }

    /**
     * Llamado desde TrackingController en cada ping de GPS. Guarda la ultima
     * posicion conocida para que un panel que recien abre pueda pedirla via
     * REST (ver obtenerUbicacionesActivas) antes de suscribirse al topic en vivo.
     */
    public void actualizarUltimaUbicacion(Long repartidorId, UbicacionDTO ubicacion) {
        ultimasUbicaciones.put(repartidorId, ubicacion);
    }

    /**
     * Snapshot de las ultimas posiciones conocidas de todos los repartidores
     * que tienen sesion activa ahora mismo. Pensado para que el panel admin
     * pinte el mapa inicial antes de suscribirse a /topic/tracking/{id}.
     */
    public Collection<UbicacionDTO> obtenerUbicacionesActivas() {
        return ultimasUbicaciones.values();
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
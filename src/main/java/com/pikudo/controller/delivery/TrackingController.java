package com.pikudo.controller.delivery;

import com.pikudo.dto.tracking.UbicacionDTO;
import com.pikudo.dto.tracking.UbicacionEntranteDTO;
import com.pikudo.dto.delivery.DeliveryLocationRequestDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.delivery.DeliveryTrackingService;
import com.pikudo.service.impl.PresenciaRepartidorServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

// CORRECCIÓN: Este es el import correcto para validaciones en WebSockets/STOMP
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class TrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenciaRepartidorServiceImpl presenciaService;
    private final UsuarioRepository usuarioRepository;
    private final DeliveryTrackingService deliveryTrackingService;

    /**
     * El celular del repartidor llama esto justo despues de conectarse.
     * Aquí hacemos la única consulta a la BD para dejar el ID guardado en la sesión.
     */
    @MessageMapping("/repartidores/conectar")
    public void conectar(Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        Usuario usuario = obtenerUsuarioAutenticado(principal);
        
        // Guardamos el ID en los atributos de la sesión WebSocket
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("repartidorId", usuario.getId());
        }
        
        presenciaService.registrarConexion(sessionId, usuario.getId());
        log.info("Repartidor {} conectado exitosamente (ID: {})", usuario.getUsername(), usuario.getId());
    }

    /**
     * Recibe el GPS continuo. Rendimiento puro: Cero golpes a la Base de Datos.
     */
    @MessageMapping("/tracking")
    public void actualizarUbicacion(@Valid UbicacionEntranteDTO entrante, SimpMessageHeaderAccessor headerAccessor) {
        // Recuperamos el ID directamente de la memoria de la sesión
        Long repartidorId = null;
        if (headerAccessor.getSessionAttributes() != null) {
            repartidorId = (Long) headerAccessor.getSessionAttributes().get("repartidorId");
        }

        // Fail-safe: Si por alguna razón el cliente mandó GPS sin conectar primero, resolvemos una sola vez
        if (repartidorId == null) {
            Principal principal = headerAccessor.getUser();
            if (principal == null) return;
            Usuario usuario = obtenerUsuarioAutenticado(principal);
            repartidorId = usuario.getId();
            if (headerAccessor.getSessionAttributes() != null) {
                headerAccessor.getSessionAttributes().put("repartidorId", repartidorId);
            }
        }

        UbicacionDTO ubicacion = new UbicacionDTO(
                repartidorId,
                entrante.getLat(),
                entrante.getLng(),
                System.currentTimeMillis()
        );

        DeliveryLocationRequestDTO request = new DeliveryLocationRequestDTO();
        request.setLatitude(java.math.BigDecimal.valueOf(entrante.getLat()));
        request.setLongitude(java.math.BigDecimal.valueOf(entrante.getLng()));
        deliveryTrackingService.reportActiveDriverLocation(repartidorId, request);

        // Mantiene compatibilidad con la carga inicial de paneles actuales.
        presenciaService.actualizarUltimaUbicacion(repartidorId, ubicacion);

        // Retransmite en vivo a los paneles de administración y caja
        messagingTemplate.convertAndSend("/topic/tracking/" + repartidorId, ubicacion);
    }

    /**
     * CORREGIDO: Ahora sí atrapa los errores de @Valid en los payloads de STOMP
     */
    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void manejarErrorValidacion(MethodArgumentNotValidException e, Principal principal) {
        String username = (principal != null) ? principal.getName() : "desconocido";
        log.warn("Ubicación GPS inválida rechazada para el usuario {}. Detalles: {}", username, e.getMessage());
    }

    private Usuario obtenerUsuarioAutenticado(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Sesión WebSocket no válida o expirada");
        }
        Authentication auth = (Authentication) principal;
        String username = auth.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado en BD: " + username));
    }
}

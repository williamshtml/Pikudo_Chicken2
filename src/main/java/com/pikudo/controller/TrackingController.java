package com.pikudo.controller;

import com.pikudo.dto.tracking.UbicacionDTO;
import com.pikudo.dto.tracking.UbicacionEntranteDTO;
import com.pikudo.entity.Usuario;
import com.pikudo.repository.UsuarioRepository;
import com.pikudo.service.impl.PresenciaRepartidorService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

/**
 * Endpoints STOMP para presencia y tracking GPS de repartidores.
 *
 * El repartidorId NUNCA se toma de lo que manda el cliente: siempre se obtiene
 * del usuario autenticado en la sesion WebSocket (Principal), que ya fue validado
 * por WebSocketAuthInterceptor al conectar. Esto evita que un repartidor pueda
 * enviar la ubicacion o marcar presencia de otro.
 */
@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final PresenciaRepartidorService presenciaService;
    private final UsuarioRepository usuarioRepository;

    /**
     * El celular del repartidor llama esto justo despues de conectarse
     * (antes de empezar a mandar ubicacion), para marcarse como "en linea".
     */
    @MessageMapping("/repartidores/conectar")
    public void conectar(Principal principal, @Header("simpSessionId") String sessionId) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);
        presenciaService.registrarConexion(sessionId, usuario.getId());
    }

    /**
     * El celular del repartidor manda su posicion GPS aqui, cada X segundos.
     * El backend la retransmite a quienes esten escuchando /topic/tracking/{repartidorId}.
     */
    @MessageMapping("/tracking")
    public void actualizarUbicacion(UbicacionEntranteDTO entrante, Principal principal) {
        Usuario usuario = obtenerUsuarioAutenticado(principal);

        UbicacionDTO ubicacion = new UbicacionDTO(
                usuario.getId(),
                entrante.getLat(),
                entrante.getLng(),
                System.currentTimeMillis()
        );

        messagingTemplate.convertAndSend("/topic/tracking/" + usuario.getId(), ubicacion);
    }

    private Usuario obtenerUsuarioAutenticado(Principal principal) {
        // El Principal en una sesion STOMP autenticada es un Authentication de Spring Security
        Authentication auth = (Authentication) principal;
        String username = auth.getName();
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado: " + username));
    }
}
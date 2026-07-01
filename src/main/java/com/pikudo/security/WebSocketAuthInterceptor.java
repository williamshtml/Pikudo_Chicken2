package com.pikudo.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Valida el JWT en el momento en que un cliente (celular del repartidor, PC de caja)
 * intenta conectarse al WebSocket via STOMP.
 *
 * Se engancha aparte de JwtFilter porque JwtFilter solo protege peticiones HTTP normales;
 * el handshake de WebSocket necesita su propia validacion a nivel de mensaje CONNECT.
 *
 * El token se espera en el header STOMP "Authorization: Bearer <token>",
 * que el cliente (Angular) debe enviar al conectar, igual que en las peticiones REST.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Conexion WebSocket rechazada: falta el header Authorization");
                throw new org.springframework.messaging.simp.stomp.StompConversionException(
                        "Token JWT requerido para conectar");
            }

            String jwt = authHeader.substring(7);
            String username = jwtService.extractUsername(jwt);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(jwt, userDetails)) {
                log.warn("Conexion WebSocket rechazada: token invalido o expirado para {}", username);
                throw new org.springframework.messaging.simp.stomp.StompConversionException(
                        "Token JWT invalido o expirado");
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());

            // Se asocia el usuario autenticado a esta sesion STOMP especifica.
            // A partir de aqui, accessor.getUser() estara disponible en toda la sesion.
            accessor.setUser(authToken);

            log.info("Conexion WebSocket autenticada correctamente: {}", username);
        }

        return message;
    }
}
package com.pikudo.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Component

public class JwtFilter extends OncePerRequestFilter{
    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    /*
     Intercepta cada petición HTTP entrante, lee la cabecera Authorization,
     valida el token Bearer y establece la identidad en el contexto de Spring.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Si la cabecera no existe o no empieza con "Bearer ", ignoramos el filtro y continuamos
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // El texto "Bearer " tiene 7 caracteres, así que cortamos el String desde el índice 7 en adelante
        jwt = authHeader.substring(7);
        username = jwtService.extractUsername(jwt);

        // Si encontramos un username y el usuario no está autenticado aún en el contexto actual
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Si el token criptográfico coincide con los datos del usuario de la BD y no ha expirado
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                // 🔥 ESTABLECE LA IDENTIDAD: Guardamos al usuario en el contexto de Spring para esta petición
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // Enviamos la petición al siguiente eslabón de la cadena (el controlador final)
        filterChain.doFilter(request, response);
    }
}

package com.pikudo.config;

import com.pikudo.security.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // <-- Esto activa la seguridad por roles (@PreAuthorize) en tus controladores
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    // Inyectamos el filtro JWT mediante constructor
    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /*
     Define la cadena de filtros de seguridad. Aquí le indicamos a Spring qué rutas
     son públicas (como el Login) y cuáles requieren token obligatorio.
     */
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF para APIs REST
        .authorizeHttpRequests(auth -> auth
            // 1. Permite acceso público a la documentación de Swagger
            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
            // 2. Permite acceso público a tus rutas de login/auth (ajustado a /api/auth)
            .requestMatchers("/api/auth/**").permitAll()
            // 3. Todo lo demás requiere estar autenticado
            .anyRequest().authenticated()
        )
        // 4. Configuración Stateless (necesaria para JWT)
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        // 5. Agregamos tu filtro de seguridad JWT
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
}
    /*
     Bean encargado de encriptar las contraseñas en la base de datos usando el algoritmo BCrypt.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     Administrador de autenticación requerido para procesar el Login en los servicios.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
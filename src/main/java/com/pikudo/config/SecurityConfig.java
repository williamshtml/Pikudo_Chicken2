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
        http
            // Deshabilitamos CSRF ya que las APIs REST con tokens no lo necesitan
            .csrf(csrf -> csrf.disable())
            
            // Configuración de rutas protegidas
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: login, websockets y documentación Swagger/OpenAPI
                .requestMatchers(
                        "/api/auth/**", 
                        "/ws-pikudo/**",
                        "/v3/api-docs/**",     // <-- Necesario para que Postman pueda importar el JSON
                        "/swagger-ui/**",      // <-- Necesario para ver la web de Swagger
                        "/swagger-ui.html"
                ).permitAll()
                .anyRequest().authenticated() // Cualquier otra petición requiere token válido
            )
            
            // Indicamos que nuestra API es Stateless (sin estado/sin sesiones en el servidor)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Enganchamos nuestro filtro JWT antes del filtro de autenticación por defecto de Spring
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
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
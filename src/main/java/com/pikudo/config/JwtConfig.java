package com.pikudo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter

public class JwtConfig {
    /*
     * Lee la propiedad 'jwt.secret' desde tu archivo application.yml o application.properties.
     * Esta es la firma criptográfica con la que se aseguran los tokens de la pollería.
     */
    @Value("${jwt.secret}")
    private String secret;

    /*
     * Lee la propiedad 'jwt.expiration' (normalmente expresada en milisegundos).
     * Define cuánto tiempo será válido el token antes de que el mesero deba loguearse otra vez.
     */
    @Value("${jwt.expiration:86400000}") // Por defecto 24 horas (en milisegundos)
    private long expiration;
}

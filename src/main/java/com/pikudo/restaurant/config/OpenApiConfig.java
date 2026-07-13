package com.pikudo.restaurant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration

public class OpenApiConfig {
    /*
     Configuración global de OpenAPI/Swagger para el sistema de la pollería.
     Define el título, versión y añade el soporte para probar rutas protegidas con JWT.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        final String nombreEsquemaSeguridad = "bearerAuth";
        
        return new OpenAPI()
                // 1. Información General de la API de la Pollería
                .info(new Info()
                        .title("API REST - Pollería Pikudo Chicken")
                        .version("1.0.0")
                        .description("Documentación interactiva de las rutas del backend para el control de mesas, pedidos, stock y facturación.")
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")))
                
                // 2. Integrar el botón de Autorización JWT en la interfaz de Swagger
                .addSecurityItem(new SecurityRequirement().addList(nombreEsquemaSeguridad))
                .components(new Components()
                        .addSecuritySchemes(nombreEsquemaSeguridad, new SecurityScheme()
                                .name(nombreEsquemaSeguridad)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el token JWT obtenido del Login para poder probar los endpoints protegidos de los mozos y caja.")));
    }
}

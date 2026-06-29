package com.pikudo.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import java.time.Duration;

@Configuration

public class RestTemplateConfig {
    /*
     Configura y expone el Bean de RestTemplate con políticas de tiempo de espera rigurosas.
     Garantiza que las consultas externas de RUC/DNI no congelen el sistema de la pollería.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                // 1. Tiempo de espera para establecer la conexión inicial con la API externa (3 segundos)
                .setConnectTimeout(Duration.ofSeconds(3))
                
                // 2. Tiempo de espera máximo para recibir los datos de la respuesta (5 segundos)
                .setReadTimeout(Duration.ofSeconds(5))
                
                .build();
    }
}

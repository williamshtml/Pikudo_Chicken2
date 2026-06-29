package com.pikudo.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration

public class RestTemplateConfig {
    /*
     Configura y expone el Bean de RestTemplate con políticas de tiempo de espera rigurosas.
     Garantiza que las consultas externas de RUC/DNI no congelen el sistema de la pollería.
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        // Creamos la fábrica nativa de peticiones HTTP de Java
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        
        // Configuramos los tiempos de espera directamente en milisegundos
        factory.setConnectTimeout(3000); // 3 segundos para conectar
        factory.setReadTimeout(5000);    // 5 segundos para leer los datos del RUC

        return builder
                .requestFactory(() -> factory)
                .build();
    }
}

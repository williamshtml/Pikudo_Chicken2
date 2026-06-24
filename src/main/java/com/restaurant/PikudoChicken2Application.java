package com.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.restaurant", "com.pikudo"})
@EnableJpaRepositories(basePackages = "com.pikudo.repository")
@EntityScan(basePackages = "com.pikudo.entity")
public class PikudoChicken2Application {

    public static void main(String[] args) {
        SpringApplication.run(PikudoChicken2Application.class, args);
    }

}
package com.example.ifrotas.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner seedDatabase() {
        return args -> {
            // O sistema inicia completamente vazio a cada restart do servidor, conforme solicitado.
        };
    }
}

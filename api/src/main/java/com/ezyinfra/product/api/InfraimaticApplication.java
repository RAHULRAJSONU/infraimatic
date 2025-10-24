package com.ezyinfra.product.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point for the Infraimatic application. This class bootstrap the
 * Spring Boot application context and serves as a central configuration for
 * component scanning across all modules.
 */
@EnableJpaRepositories(basePackages = "com.ezyinfra.product")
@EntityScan(basePackages = "com.ezyinfra.product")
@SpringBootApplication(scanBasePackages = "com.ezyinfra.product")
public class InfraimaticApplication {
    public static void main(String[] args) {
        SpringApplication.run(InfraimaticApplication.class, args);
    }
}
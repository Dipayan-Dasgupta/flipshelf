package com.flipshelf;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;

@SpringBootApplication
@EnableFeignClients
@EnableMethodSecurity
@EnableAsync
public class FlipshelfProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlipshelfProductApplication.class, args);
    }
}

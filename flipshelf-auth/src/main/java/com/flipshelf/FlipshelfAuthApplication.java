package com.flipshelf;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.Base64;

@SpringBootApplication
@EnableMethodSecurity
public class FlipshelfAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlipshelfAuthApplication.class, args);
	}

}

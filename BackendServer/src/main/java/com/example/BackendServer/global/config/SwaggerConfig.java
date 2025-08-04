package com.example.BackendServer.global.config;

import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import static io.swagger.v3.oas.models.security.SecurityScheme.Type.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI customOpenAPI() {
		SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");
		Components components = new Components().addSecuritySchemes("bearerAuth",
			new SecurityScheme().name("bearerAuth").type(HTTP)
				.scheme("bearer").bearerFormat("JWT"));
		return new OpenAPI()
			.info(new Info()
				.title("GPS Tracker API")
				.version("1.0.0")
				.description("GPS Tracker API 명세서"))
			.addSecurityItem(securityRequirement)
			.components(components);
	}

	@Bean
	public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
		return b -> b.serializerByType(
				LocalDateTime.class,
				new LocalDateTimeSerializer(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
		);
	}
}

package com.example.BackendServer.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins("http://localhost:5173", "http://localhost:8000", "http://gps-tracker-fe.s3-website.ap-northeast-2.amazonaws.com")
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }
}

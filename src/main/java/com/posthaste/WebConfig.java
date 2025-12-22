package com.posthaste;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:[*]", "https://posthaste-pro-web.web.app",
                        "https://posthaste-pro-app.web.app", "https://*.posthaste.pro")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

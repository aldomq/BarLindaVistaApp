package com.lindavista.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configura CORS para que la web y la app movil puedan consumir la API.
 * Los origenes permitidos se leen de la propiedad app.cors.origins (CORS_ORIGIN).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Value("${app.cors.origins:*}")
  private String origins;

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    var mapping = registry.addMapping("/**")
        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        .allowedHeaders("*");

    if (origins == null || origins.isBlank() || origins.contains("*")) {
      mapping.allowedOriginPatterns("*");
    } else {
      mapping.allowedOrigins(origins.split(","));
    }
  }
}

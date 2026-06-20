package com.lindavista.common;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Endpoints informativos: raiz y healthcheck (incluye chequeo de la base de datos).
 */
@RestController
public class MetaController {

  private final DataSource dataSource;

  public MetaController(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @GetMapping("/")
  public Map<String, Object> root() {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("name", "LindaVista API");
    body.put("version", "0.1.0");
    body.put("docs", "/api/health");
    return body;
  }

  @GetMapping("/api/health")
  public Map<String, Object> health() {
    String database = "ok";
    try (Connection connection = dataSource.getConnection()) {
      if (!connection.isValid(2)) {
        database = "error";
      }
    } catch (Exception e) {
      database = "error";
    }

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", "ok");
    body.put("database", database);
    body.put("timestamp", Instant.now().toString());
    return body;
  }
}

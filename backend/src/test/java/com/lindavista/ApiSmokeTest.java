package com.lindavista;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de integracion de extremo a extremo (base en memoria H2):
 * ejecuta el flujo completo del negocio a traves de la API HTTP.
 */
@SpringBootTest
class ApiSmokeTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
  }

  private static String idOf(String json) {
    Matcher m = Pattern.compile("\"id\"\\s*:\\s*\"([^\"]+)\"").matcher(json);
    assertTrue(m.find(), "No se encontro un id en: " + json);
    return m.group(1);
  }

  @Test
  void healthEndpointResponde() throws Exception {
    String body = mockMvc.perform(get("/api/health"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    assertTrue(body.contains("\"status\":\"ok\""), body);
    assertTrue(body.contains("\"database\":\"ok\""), body);
  }

  @Test
  void flujoCompletoInventarioVentaYContabilidad() throws Exception {
    // 1. Crear categoria
    String catJson = mockMvc.perform(post("/api/categories")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Bebidas\"}"))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String categoryId = idOf(catJson);

    // 2. Crear producto con su presentacion, precio y stock 40
    String prodJson = mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Agua\",\"presentation\":\"1 Litro\",\"cost\":5,\"price\":12,\"stock\":40,"
                + "\"categoryId\":\"" + categoryId + "\"}"))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String productId = idOf(prodJson);
    assertTrue(prodJson.contains("\"margin\":7"), prodJson); // 12 - 5

    // 3. Listar productos (paginado): total = 1
    String listJson = mockMvc.perform(get("/api/products"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    assertTrue(listJson.contains("\"total\":1"), listJson);

    // 4. Vender 3 unidades -> descuenta stock, total 36.00
    mockMvc.perform(post("/api/sales")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"paymentMethod\":\"cash\",\"items\":[{\"productId\":\"" + productId
                + "\",\"quantity\":3}]}"))
        .andExpect(status().isCreated());

    // 5. El stock debe haber bajado a 37
    String afterJson = mockMvc.perform(get("/api/products/" + productId))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    assertTrue(afterJson.contains("\"stock\":37"), afterJson);

    // 6. Crear semana contable: ganancia = 8000 - 4500 = 3500
    String weekJson = mockMvc.perform(post("/api/accounting/weeks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"weekStart\":\"2026-06-08\",\"weekEnd\":\"2026-06-14\",\"revenue\":8000,\"cost\":4500}"))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();
    String weekId = idOf(weekJson);
    assertTrue(weekJson.contains("\"profit\":3500"), weekJson);

    // 7. Repartir 1500 a salarios
    mockMvc.perform(post("/api/accounting/weeks/" + weekId + "/allocations")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"type\":\"SALARIES\",\"label\":\"Sueldos\",\"amount\":1500}"))
        .andExpect(status().isCreated());

    // 8. Resumen: repartido 1500, restante 2000
    String summaryJson = mockMvc.perform(get("/api/accounting/weeks/" + weekId + "/summary"))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    assertTrue(summaryJson.contains("\"allocated\":1500"), summaryJson);
    assertTrue(summaryJson.contains("\"remaining\":2000"), summaryJson);
  }

  @Test
  void rutaInexistenteDevuelve404() throws Exception {
    mockMvc.perform(get("/api/no-existe")).andExpect(status().isNotFound());
  }

  @Test
  void validacionDevuelve400() throws Exception {
    mockMvc.perform(post("/api/products")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"description\":\"producto sin nombre\"}"))
        .andExpect(status().isBadRequest());
  }
}

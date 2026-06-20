package com.lindavista.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** DTOs del modulo de productos (inventario). */
public final class ProductDtos {

  private ProductDtos() {}

  public record CategoryRef(String id, String name) {}

  public record ProductResponse(
      String id,
      String name,
      String presentation,
      String description,
      String notes,
      List<String> tags,
      BigDecimal cost,
      BigDecimal price,
      BigDecimal margin,
      int stock,
      boolean active,
      String categoryId,
      CategoryRef category,
      Instant createdAt,
      Instant updatedAt) {}

  public record CreateProductRequest(
      @NotBlank @Size(max = 160) String name,
      @Size(max = 80) String presentation,
      @Size(max = 1000) String description,
      @Size(max = 1000) String notes,
      List<@Size(max = 40) String> tags,
      @PositiveOrZero BigDecimal cost,
      @PositiveOrZero BigDecimal price,
      Integer stock,
      Boolean active,
      String categoryId) {}

  public record UpdateProductRequest(
      @Size(max = 160) String name,
      @Size(max = 80) String presentation,
      @Size(max = 1000) String description,
      @Size(max = 1000) String notes,
      List<@Size(max = 40) String> tags,
      @PositiveOrZero BigDecimal cost,
      @PositiveOrZero BigDecimal price,
      Integer stock,
      Boolean active,
      String categoryId) {}

  public record AdjustStockRequest(
      @NotNull Integer delta,
      @Size(max = 200) String reason) {}
}

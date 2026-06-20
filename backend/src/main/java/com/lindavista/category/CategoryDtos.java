package com.lindavista.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** DTOs (objetos de entrada/salida) del modulo de categorias. */
public final class CategoryDtos {

  private CategoryDtos() {}

  public record CategoryResponse(
      String id,
      String name,
      long productCount,
      Instant createdAt,
      Instant updatedAt) {}

  public record CreateCategoryRequest(
      @NotBlank @Size(max = 80) String name) {}

  public record UpdateCategoryRequest(
      @Size(max = 80) String name) {}
}

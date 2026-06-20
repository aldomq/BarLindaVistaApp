package com.lindavista.sale;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** DTOs del modulo de ventas (base del futuro POS). */
public final class SaleDtos {

  private SaleDtos() {}

  public record SaleItemResponse(
      String id,
      String productId,
      String name,
      int quantity,
      BigDecimal unitPrice,
      BigDecimal subtotal) {}

  public record SaleResponse(
      String id,
      BigDecimal total,
      String paymentMethod,
      String note,
      Instant createdAt,
      List<SaleItemResponse> items) {}

  public record SaleItemRequest(
      @NotBlank String productId,
      @NotNull @Positive Integer quantity,
      @PositiveOrZero BigDecimal unitPrice) {}

  public record CreateSaleRequest(
      String paymentMethod,
      @Size(max = 300) String note,
      @NotEmpty List<@Valid SaleItemRequest> items) {}
}

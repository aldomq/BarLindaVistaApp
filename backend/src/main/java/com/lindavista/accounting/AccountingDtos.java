package com.lindavista.accounting;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** DTOs del modulo de contabilidad semanal. */
public final class AccountingDtos {

  private AccountingDtos() {}

  public record AllocationResponse(
      String id,
      String weekId,
      AllocationType type,
      String label,
      BigDecimal amount,
      BigDecimal percentage,
      Instant createdAt,
      Instant updatedAt) {}

  public record WeekResponse(
      String id,
      LocalDate weekStart,
      LocalDate weekEnd,
      BigDecimal revenue,
      BigDecimal cost,
      BigDecimal profit,
      String notes,
      List<AllocationResponse> allocations,
      Instant createdAt,
      Instant updatedAt) {}

  public record WeekSummaryResponse(
      String weekId,
      LocalDate weekStart,
      LocalDate weekEnd,
      BigDecimal profit,
      BigDecimal allocated,
      BigDecimal remaining,
      Map<String, BigDecimal> byType,
      List<AllocationResponse> allocations) {}

  public record CreateWeekRequest(
      @NotNull LocalDate weekStart,
      @NotNull LocalDate weekEnd,
      @PositiveOrZero BigDecimal revenue,
      @PositiveOrZero BigDecimal cost,
      @PositiveOrZero BigDecimal profit,
      @Size(max = 500) String notes) {}

  public record UpdateWeekRequest(
      LocalDate weekStart,
      LocalDate weekEnd,
      @PositiveOrZero BigDecimal revenue,
      @PositiveOrZero BigDecimal cost,
      @PositiveOrZero BigDecimal profit,
      @Size(max = 500) String notes) {}

  public record CreateAllocationRequest(
      @NotNull AllocationType type,
      @Size(max = 160) String label,
      @NotNull @PositiveOrZero BigDecimal amount,
      @PositiveOrZero BigDecimal percentage) {}

  public record UpdateAllocationRequest(
      AllocationType type,
      @Size(max = 160) String label,
      @PositiveOrZero BigDecimal amount,
      @PositiveOrZero BigDecimal percentage) {}
}

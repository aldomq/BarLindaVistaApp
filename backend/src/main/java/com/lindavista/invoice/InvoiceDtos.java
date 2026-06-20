package com.lindavista.invoice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** DTOs del modulo de facturas. */
public final class InvoiceDtos {

  private InvoiceDtos() {}

  public record InvoiceResponse(
      String id,
      LocalDate invoiceDate,
      String vendor,
      BigDecimal amount,
      String number,
      String notes,
      InvoiceSource source,
      String fileName,
      String contentType,
      String fileUrl,
      String previewUrl,
      String accountingWeekId,
      Instant createdAt,
      Instant updatedAt) {}

  public record CreateInvoiceRequest(
      @NotNull LocalDate invoiceDate,
      @NotBlank @Size(max = 160) String vendor,
      @NotNull @PositiveOrZero BigDecimal amount,
      @Size(max = 80) String number,
      @Size(max = 1000) String notes,
      String accountingWeekId,
      // Referencias al archivo: normalmente las llena la capa de almacenamiento.
      @Size(max = 255) String fileName,
      @Size(max = 100) String contentType,
      @Size(max = 500) String fileUrl,
      @Size(max = 500) String previewUrl) {}

  public record UpdateInvoiceRequest(
      LocalDate invoiceDate,
      @Size(max = 160) String vendor,
      @PositiveOrZero BigDecimal amount,
      @Size(max = 80) String number,
      @Size(max = 1000) String notes,
      String accountingWeekId,
      @Size(max = 255) String fileName,
      @Size(max = 100) String contentType,
      @Size(max = 500) String fileUrl,
      @Size(max = 500) String previewUrl) {}
}

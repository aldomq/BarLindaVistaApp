package com.lindavista.invoice;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Factura/recibo guardado por fecha. Por ahora solo el modelo de datos:
 * el archivo real (PDF/foto) y su preview los guardará la otra persona en S3
 * y aquí solo se referencian (fileUrl / previewUrl). La captura por correo
 * también la conectará después (ver InvoiceController.emailInbound).
 */
@Entity
@Table(name = "invoices", indexes = {@Index(name = "idx_invoice_date", columnList = "invoice_date")})
@Getter
@Setter
public class Invoice {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  /** Fecha de la factura (por la que se ordena y filtra). */
  @Column(name = "invoice_date", nullable = false)
  private LocalDate invoiceDate;

  /** Proveedor que emitió la factura. */
  @Column(nullable = false, length = 160)
  private String vendor;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount = BigDecimal.ZERO;

  /** Folio/número de la factura (opcional). */
  @Column(length = 80)
  private String number;

  @Column(length = 1000)
  private String notes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private InvoiceSource source = InvoiceSource.UPLOAD;

  // ---- Archivo y preview: los llenará la capa de almacenamiento (S3) a futuro ----
  @Column(name = "file_name", length = 255)
  private String fileName;

  @Column(name = "content_type", length = 100)
  private String contentType;

  /** URL/clave del archivo en el almacenamiento (S3). */
  @Column(name = "file_url", length = 500)
  private String fileUrl;

  /** URL/clave de la miniatura para el preview. */
  @Column(name = "preview_url", length = 500)
  private String previewUrl;

  /** Enlace opcional (suelto) a una semana contable. */
  @Column(name = "accounting_week_id", length = 36)
  private String accountingWeekId;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}

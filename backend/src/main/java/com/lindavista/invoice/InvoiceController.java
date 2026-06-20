package com.lindavista.invoice;

import com.lindavista.common.PageResponse;
import com.lindavista.invoice.InvoiceDtos.CreateInvoiceRequest;
import com.lindavista.invoice.InvoiceDtos.InvoiceResponse;
import com.lindavista.invoice.InvoiceDtos.UpdateInvoiceRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceController {

  private final InvoiceService service;

  public InvoiceController(InvoiceService service) {
    this.service = service;
  }

  /** Lista por rango de fechas (from/to en formato YYYY-MM-DD) y proveedor. */
  @GetMapping
  public PageResponse<InvoiceResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(required = false) String vendor,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return service.list(from, to, vendor, page, pageSize);
  }

  @PostMapping
  public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody CreateInvoiceRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, InvoiceSource.UPLOAD));
  }

  @GetMapping("/{id}")
  public InvoiceResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public InvoiceResponse update(@PathVariable String id, @Valid @RequestBody UpdateInvoiceRequest req) {
    return service.update(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  /**
   * BASE para la captura por correo. La idea: configurar un buzón
   * (ej. facturas@tunegocio.com) en un servicio de correo entrante (Mailgun/
   * Postmark/AWS SES) que, al recibir un correo, haga POST aquí con los datos
   * ya procesados.
   *
   * PENDIENTE para la otra persona:
   *  - Validar la firma del webhook del proveedor (seguridad).
   *  - Guardar el adjunto (PDF/foto) en S3 y generar la miniatura (preview).
   *  - Rellenar fileUrl/previewUrl/contentType con lo de S3.
   *
   * Por ahora solo crea la factura con los datos de texto y la marca como EMAIL.
   */
  @PostMapping("/email-inbound")
  public ResponseEntity<InvoiceResponse> emailInbound(@Valid @RequestBody CreateInvoiceRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req, InvoiceSource.EMAIL));
  }
}

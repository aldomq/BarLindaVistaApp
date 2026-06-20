package com.lindavista.invoice;

/**
 * De dónde vino la factura. El modelo no se amarra al correo: una factura
 * puede entrar por subida manual (web/móvil), por correo (a futuro) o por API.
 */
public enum InvoiceSource {
  UPLOAD,
  EMAIL,
  API
}

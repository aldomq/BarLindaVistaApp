package com.lindavista.user;

/**
 * Roles del sistema.
 * - ADMIN: dueño (tú). Acceso total: usuarios, inventario, contabilidad.
 * - MANAGER: persona que administra el negocio (día a día: inventario, ventas).
 *
 * En el futuro se pueden agregar más (p. ej. EMPLOYEE/cajero para el POS).
 */
public enum Role {
  ADMIN,
  MANAGER
}

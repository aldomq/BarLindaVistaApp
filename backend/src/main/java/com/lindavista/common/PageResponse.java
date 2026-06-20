package com.lindavista.common;

import java.util.List;

/**
 * Respuesta paginada estandar:
 *   { "data": [...], "pagination": { page, pageSize, total, totalPages } }
 */
public record PageResponse<T>(List<T> data, PageMeta pagination) {

  public record PageMeta(int page, int pageSize, long total, int totalPages) {}

  public static <T> PageResponse<T> of(List<T> data, long total, int page, int pageSize) {
    int totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 1;
    if (totalPages < 1) {
      totalPages = 1;
    }
    return new PageResponse<>(data, new PageMeta(page, pageSize, total, totalPages));
  }
}

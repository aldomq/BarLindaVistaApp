package com.lindavista.common;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Helpers de paginacion: paginas que empiezan en 1, tamano entre 1 y 100.
 */
public final class Paging {

  private Paging() {}

  public static int page(Integer page) {
    return (page == null || page < 1) ? 1 : page;
  }

  public static int size(Integer pageSize) {
    if (pageSize == null) {
      return 20;
    }
    return Math.min(100, Math.max(1, pageSize));
  }

  public static PageRequest of(int page, int size, Sort sort) {
    return PageRequest.of(page - 1, size, sort);
  }
}

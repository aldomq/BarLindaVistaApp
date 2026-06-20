package com.lindavista.common;

/**
 * Envoltura simple para listas no paginadas: { "data": ... }
 */
public record DataResponse<T>(T data) {}

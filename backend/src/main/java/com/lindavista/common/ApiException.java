package com.lindavista.common;

import org.springframework.http.HttpStatus;

/**
 * Excepcion de aplicacion con codigo HTTP. La captura GlobalExceptionHandler
 * y la convierte en una respuesta JSON consistente.
 */
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final transient Object details;

  public ApiException(HttpStatus status, String message, Object details) {
    super(message);
    this.status = status;
    this.details = details;
  }

  public ApiException(HttpStatus status, String message) {
    this(status, message, null);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Object getDetails() {
    return details;
  }

  public static ApiException notFound(String message) {
    return new ApiException(HttpStatus.NOT_FOUND, message);
  }

  public static ApiException badRequest(String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, message);
  }

  public static ApiException conflict(String message) {
    return new ApiException(HttpStatus.CONFLICT, message);
  }
}

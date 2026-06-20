package com.lindavista.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo central de errores. Todas las respuestas de error tienen la forma:
 *   { "error": { "message": "...", "details": ... } }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  public record ErrorBody(String message, Object details) {}

  public record ErrorResponse(ErrorBody error) {}

  private static ResponseEntity<ErrorResponse> build(HttpStatus status, String message, Object details) {
    return ResponseEntity.status(status).body(new ErrorResponse(new ErrorBody(message, details)));
  }

  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
    return build(ex.getStatus(), ex.getMessage(), ex.getDetails());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
      fields.put(fe.getField(), fe.getDefaultMessage());
    }
    return build(HttpStatus.BAD_REQUEST, "Datos invalidos", fields);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
    return build(HttpStatus.BAD_REQUEST, "JSON invalido o mal formado", null);
  }

  // Ruta inexistente -> 404 (con nuestra forma de error)
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
    return build(HttpStatus.NOT_FOUND, "Ruta no encontrada", null);
  }

  // Metodo HTTP no permitido para la ruta -> 405
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, "Metodo no permitido para esta ruta", null);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleIntegrity(DataIntegrityViolationException ex) {
    return build(
        HttpStatus.CONFLICT,
        "Conflicto: viola una restriccion de la base (valor duplicado o referencia invalida)",
        null);
  }

  // Base de datos inaccesible (DB_* mal configurado o servidor caido)
  @ExceptionHandler({CannotCreateTransactionException.class, DataAccessResourceFailureException.class})
  public ResponseEntity<ErrorResponse> handleDbDown(Exception ex) {
    log.error("No se pudo conectar a la base de datos", ex);
    return build(
        HttpStatus.SERVICE_UNAVAILABLE,
        "No se pudo conectar a la base de datos. Revisa la configuracion DB_* en el .env",
        null);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    log.error("Error no controlado", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor", null);
  }
}

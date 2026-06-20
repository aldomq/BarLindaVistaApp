package com.lindavista.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/** DTOs (objetos de entrada/salida) del modulo de usuarios. */
public final class UserDtos {

  private UserDtos() {}

  /** Respuesta: nunca incluye la contraseña. */
  public record UserResponse(
      String id,
      String name,
      String email,
      Role role,
      boolean active,
      Instant createdAt,
      Instant updatedAt) {}

  public record CreateUserRequest(
      @NotBlank @Size(max = 120) String name,
      @NotBlank @Email @Size(max = 160) String email,
      @NotBlank @Size(min = 6, max = 100) String password,
      @NotNull Role role) {}

  /** Todos los campos opcionales: solo se actualiza lo que venga. */
  public record UpdateUserRequest(
      @Size(max = 120) String name,
      @Email @Size(max = 160) String email,
      @Size(min = 6, max = 100) String password,
      Role role,
      Boolean active) {}
}

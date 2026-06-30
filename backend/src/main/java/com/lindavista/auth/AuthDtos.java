package com.lindavista.auth;

import com.lindavista.user.Role;
import jakarta.validation.constraints.NotBlank;

/** DTOs del login. */
public final class AuthDtos {

  private AuthDtos() {}

  public record LoginRequest(
      @NotBlank String username,
      @NotBlank String password) {}

  /** Respuesta del login: el token y los datos basicos del usuario. */
  public record AuthUser(String id, String name, String username, Role role) {}

  public record LoginResponse(String token, AuthUser user) {}
}

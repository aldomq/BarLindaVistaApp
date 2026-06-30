package com.lindavista;

import com.lindavista.auth.TokenService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Verifica la firma del token: valido va y vuelve, alterado o ajeno se rechaza. */
class TokenServiceTest {

  private final TokenService svc = new TokenService("secreto-de-prueba");

  @Test
  void tokenValidoDevuelveLosClaims() {
    String token = svc.issue("user-123", "ADMIN");
    TokenService.Claims c = svc.verify(token);
    assertEquals("user-123", c.userId());
    assertEquals("ADMIN", c.role());
  }

  @Test
  void tokenAlteradoSeRechaza() {
    String token = svc.issue("user-123", "MANAGER");
    // Cambiar un caracter de la firma invalida el token.
    String alterado = token.substring(0, token.length() - 1)
        + (token.endsWith("A") ? "B" : "A");
    assertNull(svc.verify(alterado));
  }

  @Test
  void tokenFirmadoConOtroSecretoSeRechaza() {
    String token = new TokenService("otro-secreto").issue("user-123", "ADMIN");
    assertNull(svc.verify(token)); // firmado con secreto distinto
  }

  @Test
  void basuraSeRechaza() {
    assertNull(svc.verify(null));
    assertNull(svc.verify("sin-punto"));
    assertNull(svc.verify("a.b"));
  }
}

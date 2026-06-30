package com.lindavista.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Token de sesion firmado con HMAC-SHA256 (solo JDK, sin librerias externas).
 * Formato: base64url(payload) + "." + base64url(firma), donde
 * payload = "userId|role|expiraEnMillis". Es stateless: no hay tabla de tokens,
 * sobrevive reinicios del servidor. La firma evita que alguien lo altere.
 *
 * ponytail: HMAC con stdlib en vez de una libreria JWT. Si algun dia se necesitan
 * claims estandar o rotacion de llaves, migrar a JJWT.
 */
@Service
public class TokenService {

  private static final long TTL_MILLIS = 1000L * 60 * 60 * 12; // 12 horas
  private final byte[] secret;

  public TokenService(@Value("${app.auth.secret:change-me-dev-secret}") String secret) {
    this.secret = secret.getBytes(StandardCharsets.UTF_8);
  }

  /** Datos que viajan dentro del token. */
  public record Claims(String userId, String role) {}

  public String issue(String userId, String role) {
    String payload = userId + "|" + role + "|" + (System.currentTimeMillis() + TTL_MILLIS);
    String p = base64(payload.getBytes(StandardCharsets.UTF_8));
    String sig = base64(sign(p));
    return p + "." + sig;
  }

  /** Devuelve los claims si el token es valido y no expiro; si no, null. */
  public Claims verify(String token) {
    if (token == null) return null;
    int dot = token.indexOf('.');
    if (dot < 0) return null;
    String p = token.substring(0, dot);
    String sig = token.substring(dot + 1);

    // Firma valida? (comparacion en tiempo constante)
    if (!MessageDigest.isEqual(unbase64(sig), sign(p))) return null;

    String payload = new String(unbase64(p), StandardCharsets.UTF_8);
    String[] parts = payload.split("\\|");
    if (parts.length != 3) return null;
    try {
      if (System.currentTimeMillis() > Long.parseLong(parts[2])) return null; // expirado
    } catch (NumberFormatException e) {
      return null;
    }
    return new Claims(parts[0], parts[1]);
  }

  private byte[] sign(String data) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret, "HmacSHA256"));
      return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    } catch (Exception e) {
      throw new IllegalStateException("No se pudo firmar el token", e);
    }
  }

  private static String base64(byte[] b) {
    return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
  }

  private static byte[] unbase64(String s) {
    try {
      return Base64.getUrlDecoder().decode(s);
    } catch (IllegalArgumentException e) {
      return new byte[0];
    }
  }
}

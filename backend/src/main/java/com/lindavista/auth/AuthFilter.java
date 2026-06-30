package com.lindavista.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticacion. Exige un token valido (Authorization: Bearer ...) en
 * todos los endpoints /api salvo el login y el health. Los endpoints de usuarios
 * (/api/users) ademas exigen rol ADMIN.
 *
 * Se desactiva con app.auth.enabled=false (lo usan las pruebas).
 */
@Component
@ConditionalOnProperty(name = "app.auth.enabled", havingValue = "true", matchIfMissing = true)
public class AuthFilter extends OncePerRequestFilter {

  private final TokenService tokens;

  public AuthFilter(TokenService tokens) {
    this.tokens = tokens;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {

    String path = req.getRequestURI();

    // Publico: preflight CORS, raiz, health y login.
    if ("OPTIONS".equalsIgnoreCase(req.getMethod())
        || path.equals("/")
        || path.equals("/api/health")
        || path.equals("/api/auth/login")
        || !path.startsWith("/api/")) {
      chain.doFilter(req, res);
      return;
    }

    String header = req.getHeader("Authorization");
    String token = (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
    TokenService.Claims claims = tokens.verify(token);

    if (claims == null) {
      reject(res, 401, "Sesión requerida o expirada");
      return;
    }

    // Gestion de usuarios: solo ADMIN.
    if (path.startsWith("/api/users") && !"ADMIN".equals(claims.role())) {
      reject(res, 403, "Solo el administrador puede gestionar usuarios");
      return;
    }

    req.setAttribute("authUserId", claims.userId());
    req.setAttribute("authRole", claims.role());
    chain.doFilter(req, res);
  }

  private void reject(HttpServletResponse res, int status, String message) throws IOException {
    res.setStatus(status);
    res.setContentType("application/json;charset=UTF-8");
    res.setHeader("Access-Control-Allow-Origin", "*"); // que el navegador vea el error, no CORS
    res.getWriter().write("{\"error\":{\"message\":\"" + message + "\"}}");
  }
}

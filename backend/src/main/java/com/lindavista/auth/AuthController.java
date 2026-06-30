package com.lindavista.auth;

import com.lindavista.auth.AuthDtos.AuthUser;
import com.lindavista.auth.AuthDtos.LoginRequest;
import com.lindavista.auth.AuthDtos.LoginResponse;
import com.lindavista.common.ApiException;
import com.lindavista.user.AppUser;
import com.lindavista.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Login (no hay registro: los usuarios los crea el ADMIN). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository users;
  private final TokenService tokens;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public AuthController(UserRepository users, TokenService tokens) {
    this.users = users;
    this.tokens = tokens;
  }

  @PostMapping("/login")
  public LoginResponse login(@Valid @RequestBody LoginRequest req) {
    AppUser u = users.findByUsernameIgnoreCase(req.username().trim()).orElse(null);
    // Mismo mensaje para usuario inexistente o password incorrecta (no filtrar info).
    if (u == null || !u.isActive() || !encoder.matches(req.password(), u.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos");
    }
    String token = tokens.issue(u.getId(), u.getRole().name());
    return new LoginResponse(token, new AuthUser(u.getId(), u.getName(), u.getUsername(), u.getRole()));
  }

  /** Devuelve el usuario del token actual (para validar la sesion al cargar la app). */
  @GetMapping("/me")
  public AuthUser me(HttpServletRequest request) {
    String userId = (String) request.getAttribute("authUserId");
    AppUser u = (userId == null) ? null : users.findById(userId).orElse(null);
    if (u == null) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión inválida");
    }
    return new AuthUser(u.getId(), u.getName(), u.getUsername(), u.getRole());
  }
}

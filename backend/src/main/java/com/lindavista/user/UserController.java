package com.lindavista.user;

import com.lindavista.common.DataResponse;
import com.lindavista.user.UserDtos.CreateUserRequest;
import com.lindavista.user.UserDtos.UpdateUserRequest;
import com.lindavista.user.UserDtos.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestión de usuarios y roles. NOTA: por ahora estos endpoints están abiertos
 * (no hay login todavía). Cuando la app móvil/otro desarrollador agregue la capa
 * de autenticación, deberán protegerse para que solo el ADMIN los use.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService service;

  public UserController(UserService service) {
    this.service = service;
  }

  @GetMapping
  public DataResponse<List<UserResponse>> list(@RequestParam(required = false) String search) {
    return new DataResponse<>(service.list(search));
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
  }

  @GetMapping("/{id}")
  public UserResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public UserResponse update(@PathVariable String id, @Valid @RequestBody UpdateUserRequest req) {
    return service.update(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}

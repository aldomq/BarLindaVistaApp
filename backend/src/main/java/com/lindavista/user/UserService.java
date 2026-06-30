package com.lindavista.user;

import com.lindavista.common.ApiException;
import com.lindavista.user.UserDtos.CreateUserRequest;
import com.lindavista.user.UserDtos.UpdateUserRequest;
import com.lindavista.user.UserDtos.UserResponse;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

  private final UserRepository repo;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public UserService(UserRepository repo) {
    this.repo = repo;
  }

  private UserResponse map(AppUser u) {
    return new UserResponse(
        u.getId(), u.getName(), u.getUsername(), u.getRole(),
        u.isActive(), u.getCreatedAt(), u.getUpdatedAt());
  }

  @Transactional(readOnly = true)
  public List<UserResponse> list(String search) {
    List<AppUser> rows = (search == null || search.isBlank())
        ? repo.findAllByOrderByNameAsc()
        : repo.findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrderByNameAsc(
            search.trim(), search.trim());
    return rows.stream().map(this::map).toList();
  }

  @Transactional(readOnly = true)
  public UserResponse get(String id) {
    return map(find(id));
  }

  private AppUser find(String id) {
    return repo.findById(id).orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
  }

  @Transactional
  public UserResponse create(CreateUserRequest req) {
    String username = req.username().trim().toLowerCase();
    if (repo.existsByUsernameIgnoreCase(username)) {
      throw ApiException.conflict("Ya existe un usuario con ese nombre");
    }
    AppUser u = new AppUser();
    u.setName(req.name().trim());
    u.setUsername(username);
    u.setPasswordHash(encoder.encode(req.password()));
    u.setRole(req.role());
    return map(repo.save(u));
  }

  @Transactional
  public UserResponse update(String id, UpdateUserRequest req) {
    AppUser u = find(id);
    if (req.name() != null) u.setName(req.name().trim());
    if (req.username() != null) {
      String username = req.username().trim().toLowerCase();
      if (!username.equalsIgnoreCase(u.getUsername()) && repo.existsByUsernameIgnoreCase(username)) {
        throw ApiException.conflict("Ya existe un usuario con ese nombre");
      }
      u.setUsername(username);
    }
    if (req.password() != null && !req.password().isBlank()) {
      u.setPasswordHash(encoder.encode(req.password()));
    }
    if (req.role() != null) u.setRole(req.role());
    if (req.active() != null) u.setActive(req.active());
    return map(repo.save(u));
  }

  @Transactional
  public void delete(String id) {
    if (!repo.existsById(id)) {
      throw ApiException.notFound("Usuario no encontrado");
    }
    repo.deleteById(id);
  }
}

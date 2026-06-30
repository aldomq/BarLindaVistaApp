package com.lindavista.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, String> {

  List<AppUser> findAllByOrderByNameAsc();

  List<AppUser> findByNameContainingIgnoreCaseOrUsernameContainingIgnoreCaseOrderByNameAsc(
      String name, String username);

  boolean existsByUsernameIgnoreCase(String username);

  Optional<AppUser> findByUsernameIgnoreCase(String username);
}

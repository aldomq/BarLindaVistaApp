package com.lindavista.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, String> {

  List<AppUser> findAllByOrderByNameAsc();

  List<AppUser> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByNameAsc(
      String name, String email);

  boolean existsByEmailIgnoreCase(String email);

  Optional<AppUser> findByEmailIgnoreCase(String email);
}

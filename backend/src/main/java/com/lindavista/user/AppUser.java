package com.lindavista.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

/**
 * Usuario del sistema con un rol. Por ahora solo es el modelo de datos:
 * el login (autenticación) lo conectará después la app móvil/otro desarrollador.
 * La contraseña se guarda SIEMPRE encriptada (BCrypt), nunca en texto plano.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class AppUser {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 120)
  private String name;

  @Column(nullable = false, unique = true, length = 160)
  private String email;

  /** Hash BCrypt de la contraseña. Nunca se devuelve en la API. */
  @Column(nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role = Role.MANAGER;

  @Column(nullable = false)
  private boolean active = true;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}

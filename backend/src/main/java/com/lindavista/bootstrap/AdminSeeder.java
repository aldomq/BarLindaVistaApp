package com.lindavista.bootstrap;

import com.lindavista.user.AppUser;
import com.lindavista.user.Role;
import com.lindavista.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el usuario ADMIN inicial a partir de variables de entorno
 * (ADMIN_EMAIL / ADMIN_PASSWORD) SOLO si todavia no hay ningun usuario.
 * Asi la contraseña del admin vive en el entorno (Railway/.env), nunca en el repo.
 * No hay registro publico: los demas usuarios los crea el admin desde la app.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

  private final UserRepository users;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();
  private final String adminEmail;
  private final String adminPassword;

  public AdminSeeder(UserRepository users,
                     @Value("${app.admin.email:}") String adminEmail,
                     @Value("${app.admin.password:}") String adminPassword) {
    this.users = users;
    this.adminEmail = adminEmail;
    this.adminPassword = adminPassword;
  }

  @Override
  public void run(String... args) {
    if (users.count() > 0) {
      return;
    }
    if (adminEmail.isBlank() || adminPassword.isBlank()) {
      log.warn("No hay usuarios y faltan ADMIN_EMAIL/ADMIN_PASSWORD: no se creo el admin inicial.");
      return;
    }
    AppUser admin = new AppUser();
    admin.setName("Administrador");
    admin.setEmail(adminEmail.trim().toLowerCase());
    admin.setPasswordHash(encoder.encode(adminPassword));
    admin.setRole(Role.ADMIN);
    users.save(admin);
    log.info("Admin inicial creado: {}", admin.getEmail());
  }
}

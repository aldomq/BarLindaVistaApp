package com.lindavista.bootstrap;

import com.lindavista.accounting.AccountingWeek;
import com.lindavista.accounting.AccountingWeekRepository;
import com.lindavista.accounting.Allocation;
import com.lindavista.accounting.AllocationType;
import com.lindavista.category.Category;
import com.lindavista.category.CategoryRepository;
import com.lindavista.product.Product;
import com.lindavista.product.ProductRepository;
import com.lindavista.user.AppUser;
import com.lindavista.user.Role;
import com.lindavista.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Siembra datos de ejemplo al arrancar SOLO si app.seed.enabled=true (SEED_ON_START)
 * y la base esta vacia. Util para probar rapido.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

  private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

  private final CategoryRepository categoryRepo;
  private final ProductRepository productRepo;
  private final AccountingWeekRepository weekRepo;
  private final UserRepository userRepo;
  private final PasswordEncoder encoder = new BCryptPasswordEncoder();

  public DataSeeder(CategoryRepository categoryRepo, ProductRepository productRepo,
                    AccountingWeekRepository weekRepo, UserRepository userRepo) {
    this.categoryRepo = categoryRepo;
    this.productRepo = productRepo;
    this.weekRepo = weekRepo;
    this.userRepo = userRepo;
  }

  @Override
  public void run(String... args) {
    seedUsers();
    seedCatalog();
  }

  /** Crea usuarios de ejemplo (admin + encargado) si aún no hay usuarios. */
  private void seedUsers() {
    if (userRepo.count() > 0) {
      return;
    }
    log.info("Sembrando usuarios de ejemplo (admin + encargado)...");
    userRepo.save(user("Administrador", "admin", "admin1234", Role.ADMIN));
    userRepo.save(user("Encargado", "encargado", "manager1234", Role.MANAGER));
    log.info("Usuarios creados. CAMBIA estas contraseñas de ejemplo.");
  }

  private void seedCatalog() {
    if (categoryRepo.count() > 0) {
      log.info("Seed de catálogo omitido: ya hay datos.");
      return;
    }
    log.info("Sembrando datos de ejemplo...");

    // Datos de ejemplo de un BAR. Cada producto lleva su presentación,
    // precio y stock. El frontend ofrece las presentaciones como opciones.
    Category cervezas = categoryRepo.save(category("Cervezas"));
    Category licores = categoryRepo.save(category("Licores"));
    Category cocteles = categoryRepo.save(category("Cócteles"));
    Category sinAlcohol = categoryRepo.save(category("Sin alcohol"));
    Category botanas = categoryRepo.save(category("Botanas"));

    productRepo.save(product("Corona", "Botella 355ml", 18, 35, 120, cervezas));
    productRepo.save(product("Corona", "Caguama 940ml", 28, 55, 40, cervezas));
    productRepo.save(product("Modelo Especial", "Lata 355ml", 17, 33, 96, cervezas));
    productRepo.save(product("Heineken", "Botella 355ml", 22, 42, 60, cervezas));
    productRepo.save(product("Tequila Don Julio", "Caballito (shot)", 35, 90, 200, licores));
    productRepo.save(product("Tequila Don Julio", "Botella 750ml", 480, 950, 12, licores));
    productRepo.save(product("Whisky Buchanan's", "Trago (shot)", 40, 100, 150, licores));
    productRepo.save(product("Margarita", "Copa", 30, 85, 0, cocteles));
    productRepo.save(product("Michelada", "Tarro", 25, 65, 0, cocteles));
    productRepo.save(product("Refresco", "Vaso", 6, 25, 0, sinAlcohol));
    productRepo.save(product("Refresco", "Lata 355ml", 9, 30, 48, sinAlcohol));
    productRepo.save(product("Agua embotellada", "Botella 600ml", 4, 20, 60, sinAlcohol));
    productRepo.save(product("Alitas", "Orden 6 pz", 35, 90, 0, botanas));
    productRepo.save(product("Nachos", "Orden", 28, 75, 0, botanas));

    AccountingWeek week = new AccountingWeek();
    week.setWeekStart(LocalDate.of(2026, 6, 8));
    week.setWeekEnd(LocalDate.of(2026, 6, 14));
    week.setRevenue(new BigDecimal("8000.00"));
    week.setCost(new BigDecimal("4500.00"));
    week.setProfit(new BigDecimal("3500.00"));
    week.setNotes("Semana de ejemplo");
    week.addAllocation(allocation(AllocationType.SALARIES, "Sueldos", 1500));
    week.addAllocation(allocation(AllocationType.RESTOCK, "Recompra de inventario", 1200));
    week.addAllocation(allocation(AllocationType.SAVINGS, "Ahorro / mejoras", 800));
    weekRepo.save(week);

    log.info("Seed completado: categorias, productos y 1 semana contable.");
  }

  private AppUser user(String name, String username, String rawPassword, Role role) {
    AppUser u = new AppUser();
    u.setName(name);
    u.setUsername(username);
    u.setPasswordHash(encoder.encode(rawPassword));
    u.setRole(role);
    return u;
  }

  private Category category(String name) {
    Category c = new Category();
    c.setName(name);
    return c;
  }

  private Product product(String name, String presentation, double cost, double price, int stock, Category category) {
    Product p = new Product();
    p.setName(name);
    p.setPresentation(presentation);
    p.setCost(BigDecimal.valueOf(cost));
    p.setPrice(BigDecimal.valueOf(price));
    p.setStock(stock);
    p.setCategory(category);
    return p;
  }

  private Allocation allocation(AllocationType type, String label, double amount) {
    Allocation a = new Allocation();
    a.setType(type);
    a.setLabel(label);
    a.setAmount(BigDecimal.valueOf(amount));
    return a;
  }
}

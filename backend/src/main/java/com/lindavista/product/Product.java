package com.lindavista.product;

import com.lindavista.category.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "products", indexes = {@Index(name = "idx_product_name", columnList = "name")})
@Getter
@Setter
public class Product {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @Column(nullable = false, length = 160)
  private String name;

  /**
   * Presentación (ej. "Botella 355ml", "Lata", "Caballito"). Es un texto simple:
   * el frontend ofrece opciones para elegir. Cada producto lleva su propio precio
   * y stock según su presentación.
   */
  @Column(length = 80)
  private String presentation;

  @Column(length = 1000)
  private String description;

  /** Notas/comentarios internos (ej. "revisar caducidad", "favorito de la casa"). */
  @Column(length = 1000)
  private String notes;

  /** Etiquetas custom (texto libre) para clasificar/filtrar (ej. "Promoción", "Temporada"). */
  @ElementCollection
  @CollectionTable(name = "product_tags", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "tag", length = 40)
  private Set<String> tags = new LinkedHashSet<>();

  // Dinero en BigDecimal para evitar errores de redondeo.
  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal cost = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal price = BigDecimal.ZERO;

  @Column(nullable = false)
  private int stock = 0;

  @Column(nullable = false)
  private boolean active = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private Category category;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}

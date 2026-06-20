package com.lindavista.sale;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales", indexes = {@Index(name = "idx_sale_created", columnList = "created_at")})
@Getter
@Setter
public class Sale {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal total = BigDecimal.ZERO;

  @Column(name = "payment_method", nullable = false, length = 20)
  private String paymentMethod = "cash";

  @Column(length = 300)
  private String note;

  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SaleItem> items = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  public void addItem(SaleItem item) {
    item.setSale(this);
    items.add(item);
  }
}

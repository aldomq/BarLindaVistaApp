package com.lindavista.accounting;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "allocations", indexes = {@Index(name = "idx_alloc_week", columnList = "week_id")})
@Getter
@Setter
public class Allocation {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "week_id")
  private AccountingWeek week;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private AllocationType type;

  @Column(length = 160)
  private String label;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount = BigDecimal.ZERO;

  @Column(precision = 5, scale = 2)
  private BigDecimal percentage;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}

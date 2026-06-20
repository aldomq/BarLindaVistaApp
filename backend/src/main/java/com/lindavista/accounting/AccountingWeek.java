package com.lindavista.accounting;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "accounting_weeks",
    uniqueConstraints = @UniqueConstraint(name = "uq_week_range", columnNames = {"week_start", "week_end"}),
    indexes = {@Index(name = "idx_week_start", columnList = "week_start")})
@Getter
@Setter
public class AccountingWeek {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @Column(name = "week_start", nullable = false)
  private LocalDate weekStart;

  @Column(name = "week_end", nullable = false)
  private LocalDate weekEnd;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal revenue = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal cost = BigDecimal.ZERO;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal profit = BigDecimal.ZERO;

  @Column(length = 500)
  private String notes;

  @OneToMany(mappedBy = "week", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Allocation> allocations = new ArrayList<>();

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;

  public void addAllocation(Allocation allocation) {
    allocation.setWeek(this);
    allocations.add(allocation);
  }
}

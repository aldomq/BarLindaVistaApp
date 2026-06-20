package com.lindavista.category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @Column(nullable = false, unique = true, length = 80)
  private String name;

  @CreationTimestamp
  @Column(updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  private Instant updatedAt;
}

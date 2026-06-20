package com.lindavista.sale;

import com.lindavista.product.Product;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_items", indexes = {@Index(name = "idx_saleitem_sale", columnList = "sale_id")})
@Getter
@Setter
public class SaleItem {

  @Id
  @UuidGenerator
  @Column(length = 36)
  private String id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sale_id")
  private Sale sale;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id")
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private Product product;

  // Snapshot del nombre por si el producto cambia o se elimina.
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private int quantity;

  @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
  private BigDecimal unitPrice;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal subtotal;
}

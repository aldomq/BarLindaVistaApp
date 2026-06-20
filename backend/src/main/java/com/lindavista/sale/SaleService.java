package com.lindavista.sale;

import com.lindavista.common.ApiException;
import com.lindavista.common.PageResponse;
import com.lindavista.common.Paging;
import com.lindavista.product.Product;
import com.lindavista.product.ProductRepository;
import com.lindavista.sale.SaleDtos.CreateSaleRequest;
import com.lindavista.sale.SaleDtos.SaleItemRequest;
import com.lindavista.sale.SaleDtos.SaleItemResponse;
import com.lindavista.sale.SaleDtos.SaleResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class SaleService {

  private static final Set<String> PAYMENT_METHODS = Set.of("cash", "card", "transfer", "other");

  private final SaleRepository repo;
  private final ProductRepository productRepo;

  public SaleService(SaleRepository repo, ProductRepository productRepo) {
    this.repo = repo;
    this.productRepo = productRepo;
  }

  private SaleResponse map(Sale s) {
    List<SaleItemResponse> items = s.getItems().stream()
        .map(i -> new SaleItemResponse(
            i.getId(),
            i.getProduct() == null ? null : i.getProduct().getId(),
            i.getName(), i.getQuantity(), i.getUnitPrice(), i.getSubtotal()))
        .toList();
    return new SaleResponse(s.getId(), s.getTotal(), s.getPaymentMethod(), s.getNote(), s.getCreatedAt(), items);
  }

  @Transactional(readOnly = true)
  public PageResponse<SaleResponse> list(Instant from, Instant to, Integer pageParam, Integer sizeParam) {
    int page = Paging.page(pageParam);
    int size = Paging.size(sizeParam);
    Page<Sale> result = repo.search(from, to, Paging.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
    return PageResponse.of(result.getContent().stream().map(this::map).toList(), result.getTotalElements(), page, size);
  }

  @Transactional(readOnly = true)
  public SaleResponse get(String id) {
    Sale s = repo.findById(id).orElseThrow(() -> ApiException.notFound("Venta no encontrada"));
    return map(s);
  }

  /**
   * Crea una venta y descuenta el stock de cada producto de forma atomica.
   * Si algo falla (stock insuficiente, producto inexistente) se revierte todo.
   */
  @Transactional
  public SaleResponse create(CreateSaleRequest req) {
    Sale sale = new Sale();
    sale.setPaymentMethod(resolvePaymentMethod(req.paymentMethod()));
    sale.setNote(req.note());

    BigDecimal total = BigDecimal.ZERO;
    for (SaleItemRequest item : req.items()) {
      Product product = productRepo.findById(item.productId())
          .orElseThrow(() -> ApiException.badRequest("Producto no encontrado: " + item.productId()));
      String label = product.getPresentation() == null || product.getPresentation().isBlank()
          ? product.getName()
          : product.getName() + " - " + product.getPresentation();
      if (!product.isActive()) {
        throw ApiException.badRequest("Producto inactivo: " + label);
      }
      if (product.getStock() < item.quantity()) {
        throw ApiException.badRequest("Stock insuficiente de \"" + label
            + "\": hay " + product.getStock() + ", se piden " + item.quantity());
      }

      BigDecimal unitPrice = item.unitPrice() != null ? item.unitPrice() : product.getPrice();
      BigDecimal subtotal = unitPrice
          .multiply(BigDecimal.valueOf(item.quantity()))
          .setScale(2, RoundingMode.HALF_UP);
      total = total.add(subtotal);

      SaleItem saleItem = new SaleItem();
      saleItem.setProduct(product);
      saleItem.setName(label);
      saleItem.setQuantity(item.quantity());
      saleItem.setUnitPrice(unitPrice);
      saleItem.setSubtotal(subtotal);
      sale.addItem(saleItem);

      product.setStock(product.getStock() - item.quantity());
      productRepo.save(product);
    }

    sale.setTotal(total.setScale(2, RoundingMode.HALF_UP));
    return map(repo.save(sale));
  }

  private String resolvePaymentMethod(String method) {
    if (method == null || method.isBlank()) {
      return "cash";
    }
    String normalized = method.trim().toLowerCase();
    if (!PAYMENT_METHODS.contains(normalized)) {
      throw ApiException.badRequest("Metodo de pago invalido. Use: cash, card, transfer u other");
    }
    return normalized;
  }
}

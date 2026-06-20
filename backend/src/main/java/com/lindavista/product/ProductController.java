package com.lindavista.product;

import com.lindavista.common.PageResponse;
import com.lindavista.product.ProductDtos.AdjustStockRequest;
import com.lindavista.product.ProductDtos.CreateProductRequest;
import com.lindavista.product.ProductDtos.ProductResponse;
import com.lindavista.product.ProductDtos.UpdateProductRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
public class ProductController {

  private final ProductService service;

  public ProductController(ProductService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<ProductResponse> list(
      @RequestParam(required = false) String search,
      @RequestParam(required = false) String categoryId,
      @RequestParam(required = false) Boolean active,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return service.list(search, categoryId, active, page, pageSize);
  }

  @PostMapping
  public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
  }

  @GetMapping("/{id}")
  public ProductResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public ProductResponse update(@PathVariable String id, @Valid @RequestBody UpdateProductRequest req) {
    return service.update(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/adjust-stock")
  public ProductResponse adjustStock(@PathVariable String id, @Valid @RequestBody AdjustStockRequest req) {
    return service.adjustStock(id, req.delta());
  }
}

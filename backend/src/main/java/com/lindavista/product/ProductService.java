package com.lindavista.product;

import com.lindavista.category.Category;
import com.lindavista.category.CategoryRepository;
import com.lindavista.common.ApiException;
import com.lindavista.common.PageResponse;
import com.lindavista.common.Paging;
import com.lindavista.product.ProductDtos.CategoryRef;
import com.lindavista.product.ProductDtos.CreateProductRequest;
import com.lindavista.product.ProductDtos.ProductResponse;
import com.lindavista.product.ProductDtos.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
public class ProductService {

  private final ProductRepository repo;
  private final CategoryRepository categoryRepo;

  public ProductService(ProductRepository repo, CategoryRepository categoryRepo) {
    this.repo = repo;
    this.categoryRepo = categoryRepo;
  }

  private ProductResponse map(Product p) {
    BigDecimal cost = p.getCost() == null ? BigDecimal.ZERO : p.getCost();
    BigDecimal price = p.getPrice() == null ? BigDecimal.ZERO : p.getPrice();
    Category cat = p.getCategory();
    CategoryRef ref = cat == null ? null : new CategoryRef(cat.getId(), cat.getName());
    String categoryId = cat == null ? null : cat.getId();
    return new ProductResponse(
        p.getId(), p.getName(), p.getPresentation(), p.getDescription(),
        p.getNotes(), new ArrayList<>(p.getTags()),
        cost, price, price.subtract(cost),
        p.getStock(), p.isActive(),
        categoryId, ref, p.getCreatedAt(), p.getUpdatedAt());
  }

  @Transactional(readOnly = true)
  public PageResponse<ProductResponse> list(
      String search, String categoryId, Boolean active,
      Integer pageParam, Integer sizeParam) {
    int page = Paging.page(pageParam);
    int size = Paging.size(sizeParam);
    String s = (search == null || search.isBlank()) ? null : search.trim();
    String c = (categoryId == null || categoryId.isBlank()) ? null : categoryId;

    Page<Product> result = repo.search(
        s, c, active,
        Paging.of(page, size, Sort.by(Sort.Direction.ASC, "name")));

    return PageResponse.of(
        result.getContent().stream().map(this::map).toList(),
        result.getTotalElements(), page, size);
  }

  @Transactional(readOnly = true)
  public ProductResponse get(String id) {
    return map(find(id));
  }

  private Product find(String id) {
    return repo.findById(id).orElseThrow(() -> ApiException.notFound("Producto no encontrado"));
  }

  @Transactional
  public ProductResponse create(CreateProductRequest req) {
    Product p = new Product();
    p.setName(req.name().trim());
    if (req.presentation() != null && !req.presentation().isBlank()) p.setPresentation(req.presentation().trim());
    p.setDescription(req.description());
    p.setNotes(req.notes());
    if (req.tags() != null) p.setTags(new LinkedHashSet<>(req.tags()));
    if (req.cost() != null) p.setCost(req.cost());
    if (req.price() != null) p.setPrice(req.price());
    if (req.stock() != null) p.setStock(req.stock());
    if (req.active() != null) p.setActive(req.active());
    p.setCategory(resolveCategory(req.categoryId()));
    return map(repo.save(p));
  }

  @Transactional
  public ProductResponse update(String id, UpdateProductRequest req) {
    Product p = find(id);
    if (req.name() != null) p.setName(req.name().trim());
    if (req.presentation() != null) p.setPresentation(req.presentation().isBlank() ? null : req.presentation().trim());
    if (req.description() != null) p.setDescription(req.description());
    if (req.notes() != null) p.setNotes(req.notes());
    if (req.tags() != null) p.setTags(new LinkedHashSet<>(req.tags()));
    if (req.cost() != null) p.setCost(req.cost());
    if (req.price() != null) p.setPrice(req.price());
    if (req.stock() != null) p.setStock(req.stock());
    if (req.active() != null) p.setActive(req.active());
    if (req.categoryId() != null) p.setCategory(resolveCategory(req.categoryId()));
    return map(repo.save(p));
  }

  @Transactional
  public void delete(String id) {
    if (!repo.existsById(id)) {
      throw ApiException.notFound("Producto no encontrado");
    }
    repo.deleteById(id);
  }

  /** Suma o resta stock de forma segura (no permite stock negativo). */
  @Transactional
  public ProductResponse adjustStock(String id, int delta) {
    Product p = find(id);
    int newStock = p.getStock() + delta;
    if (newStock < 0) {
      throw ApiException.badRequest(
          "Stock insuficiente: hay " + p.getStock() + " y se intenta restar " + Math.abs(delta));
    }
    p.setStock(newStock);
    return map(repo.save(p));
  }

  private Category resolveCategory(String categoryId) {
    if (categoryId == null || categoryId.isBlank()) {
      return null;
    }
    return categoryRepo.findById(categoryId)
        .orElseThrow(() -> ApiException.badRequest("Categoria no encontrada: " + categoryId));
  }
}

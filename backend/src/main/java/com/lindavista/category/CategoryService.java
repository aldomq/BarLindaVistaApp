package com.lindavista.category;

import com.lindavista.category.CategoryDtos.CategoryResponse;
import com.lindavista.category.CategoryDtos.CreateCategoryRequest;
import com.lindavista.category.CategoryDtos.UpdateCategoryRequest;
import com.lindavista.common.ApiException;
import com.lindavista.product.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

  private final CategoryRepository repo;
  private final ProductRepository productRepo;

  public CategoryService(CategoryRepository repo, ProductRepository productRepo) {
    this.repo = repo;
    this.productRepo = productRepo;
  }

  private CategoryResponse map(Category c) {
    long count = productRepo.countByCategory_Id(c.getId());
    return new CategoryResponse(c.getId(), c.getName(), count, c.getCreatedAt(), c.getUpdatedAt());
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> list(String search) {
    List<Category> rows = (search == null || search.isBlank())
        ? repo.findAllByOrderByNameAsc()
        : repo.findByNameContainingIgnoreCaseOrderByNameAsc(search.trim());
    return rows.stream().map(this::map).toList();
  }

  @Transactional(readOnly = true)
  public CategoryResponse get(String id) {
    Category c = repo.findById(id).orElseThrow(() -> ApiException.notFound("Categoria no encontrada"));
    return map(c);
  }

  @Transactional
  public CategoryResponse create(CreateCategoryRequest req) {
    Category c = new Category();
    c.setName(req.name().trim());
    return map(repo.save(c));
  }

  @Transactional
  public CategoryResponse update(String id, UpdateCategoryRequest req) {
    Category c = repo.findById(id).orElseThrow(() -> ApiException.notFound("Categoria no encontrada"));
    if (req.name() != null) {
      c.setName(req.name().trim());
    }
    return map(repo.save(c));
  }

  @Transactional
  public void delete(String id) {
    if (!repo.existsById(id)) {
      throw ApiException.notFound("Categoria no encontrada");
    }
    repo.deleteById(id);
  }
}

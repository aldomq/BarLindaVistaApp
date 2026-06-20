package com.lindavista.category;

import com.lindavista.category.CategoryDtos.CategoryResponse;
import com.lindavista.category.CategoryDtos.CreateCategoryRequest;
import com.lindavista.category.CategoryDtos.UpdateCategoryRequest;
import com.lindavista.common.DataResponse;
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

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

  private final CategoryService service;

  public CategoryController(CategoryService service) {
    this.service = service;
  }

  @GetMapping
  public DataResponse<List<CategoryResponse>> list(@RequestParam(required = false) String search) {
    return new DataResponse<>(service.list(search));
  }

  @PostMapping
  public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
  }

  @GetMapping("/{id}")
  public CategoryResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PatchMapping("/{id}")
  public CategoryResponse update(@PathVariable String id, @Valid @RequestBody UpdateCategoryRequest req) {
    return service.update(id, req);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}

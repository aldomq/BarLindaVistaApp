package com.lindavista.category;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, String> {

  List<Category> findAllByOrderByNameAsc();

  List<Category> findByNameContainingIgnoreCaseOrderByNameAsc(String name);
}

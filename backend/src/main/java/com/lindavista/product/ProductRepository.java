package com.lindavista.product;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, String> {

  long countByCategory_Id(String categoryId);

  @Query("""
      select p from Product p
      where (:search is null
              or lower(p.name) like lower(concat('%', cast(:search as string), '%')))
        and (:categoryId is null or p.category.id = :categoryId)
        and (:active is null or p.active = :active)
      """)
  Page<Product> search(
      @Param("search") String search,
      @Param("categoryId") String categoryId,
      @Param("active") Boolean active,
      Pageable pageable);
}

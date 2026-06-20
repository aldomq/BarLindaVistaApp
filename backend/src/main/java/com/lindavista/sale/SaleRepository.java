package com.lindavista.sale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface SaleRepository extends JpaRepository<Sale, String> {

  @Query("""
      select s from Sale s
      where (cast(:from as timestamp) is null or s.createdAt >= :from)
        and (cast(:to as timestamp) is null or s.createdAt <= :to)
      """)
  Page<Sale> search(@Param("from") Instant from, @Param("to") Instant to, Pageable pageable);
}

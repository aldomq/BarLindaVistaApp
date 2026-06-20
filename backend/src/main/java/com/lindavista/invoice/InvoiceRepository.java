package com.lindavista.invoice;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface InvoiceRepository extends JpaRepository<Invoice, String> {

  @Query("""
      select i from Invoice i
      where (cast(:from as date) is null or i.invoiceDate >= :from)
        and (cast(:to as date) is null or i.invoiceDate <= :to)
        and (cast(:vendor as string) is null
              or lower(i.vendor) like lower(concat('%', cast(:vendor as string), '%')))
      """)
  Page<Invoice> search(
      @Param("from") LocalDate from,
      @Param("to") LocalDate to,
      @Param("vendor") String vendor,
      Pageable pageable);
}

package com.lindavista.sale;

import com.lindavista.common.PageResponse;
import com.lindavista.sale.SaleDtos.CreateSaleRequest;
import com.lindavista.sale.SaleDtos.SaleResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

  private final SaleService service;

  public SaleController(SaleService service) {
    this.service = service;
  }

  @GetMapping
  public PageResponse<SaleResponse> list(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return service.list(from, to, page, pageSize);
  }

  @PostMapping
  public ResponseEntity<SaleResponse> create(@Valid @RequestBody CreateSaleRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
  }

  @GetMapping("/{id}")
  public SaleResponse get(@PathVariable String id) {
    return service.get(id);
  }
}

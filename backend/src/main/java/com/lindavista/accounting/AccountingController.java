package com.lindavista.accounting;

import com.lindavista.accounting.AccountingDtos.AllocationResponse;
import com.lindavista.accounting.AccountingDtos.CreateAllocationRequest;
import com.lindavista.accounting.AccountingDtos.CreateWeekRequest;
import com.lindavista.accounting.AccountingDtos.UpdateAllocationRequest;
import com.lindavista.accounting.AccountingDtos.UpdateWeekRequest;
import com.lindavista.accounting.AccountingDtos.WeekResponse;
import com.lindavista.accounting.AccountingDtos.WeekSummaryResponse;
import com.lindavista.common.PageResponse;
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
@RequestMapping("/api/accounting")
public class AccountingController {

  private final AccountingService service;

  public AccountingController(AccountingService service) {
    this.service = service;
  }

  // ---------- semanas ----------

  @GetMapping("/weeks")
  public PageResponse<WeekResponse> listWeeks(
      @RequestParam(required = false) Integer page,
      @RequestParam(required = false) Integer pageSize) {
    return service.listWeeks(page, pageSize);
  }

  @PostMapping("/weeks")
  public ResponseEntity<WeekResponse> createWeek(@Valid @RequestBody CreateWeekRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.createWeek(req));
  }

  @GetMapping("/weeks/{id}")
  public WeekResponse getWeek(@PathVariable String id) {
    return service.getWeek(id);
  }

  @PatchMapping("/weeks/{id}")
  public WeekResponse updateWeek(@PathVariable String id, @Valid @RequestBody UpdateWeekRequest req) {
    return service.updateWeek(id, req);
  }

  @DeleteMapping("/weeks/{id}")
  public ResponseEntity<Void> removeWeek(@PathVariable String id) {
    service.removeWeek(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/weeks/{id}/summary")
  public WeekSummaryResponse summary(@PathVariable String id) {
    return service.summary(id);
  }

  // ---------- asignaciones (reparto de la ganancia) ----------

  @PostMapping("/weeks/{id}/allocations")
  public ResponseEntity<AllocationResponse> addAllocation(
      @PathVariable String id, @Valid @RequestBody CreateAllocationRequest req) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.addAllocation(id, req));
  }

  @PatchMapping("/allocations/{allocationId}")
  public AllocationResponse updateAllocation(
      @PathVariable String allocationId, @Valid @RequestBody UpdateAllocationRequest req) {
    return service.updateAllocation(allocationId, req);
  }

  @DeleteMapping("/allocations/{allocationId}")
  public ResponseEntity<Void> removeAllocation(@PathVariable String allocationId) {
    service.removeAllocation(allocationId);
    return ResponseEntity.noContent().build();
  }
}

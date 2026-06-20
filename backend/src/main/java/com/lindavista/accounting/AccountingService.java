package com.lindavista.accounting;

import com.lindavista.accounting.AccountingDtos.AllocationResponse;
import com.lindavista.accounting.AccountingDtos.CreateAllocationRequest;
import com.lindavista.accounting.AccountingDtos.CreateWeekRequest;
import com.lindavista.accounting.AccountingDtos.UpdateAllocationRequest;
import com.lindavista.accounting.AccountingDtos.UpdateWeekRequest;
import com.lindavista.accounting.AccountingDtos.WeekResponse;
import com.lindavista.accounting.AccountingDtos.WeekSummaryResponse;
import com.lindavista.common.ApiException;
import com.lindavista.common.PageResponse;
import com.lindavista.common.Paging;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccountingService {

  private final AccountingWeekRepository weekRepo;
  private final AllocationRepository allocationRepo;

  public AccountingService(AccountingWeekRepository weekRepo, AllocationRepository allocationRepo) {
    this.weekRepo = weekRepo;
    this.allocationRepo = allocationRepo;
  }

  // ---------- mapeo ----------

  private AllocationResponse mapAllocation(Allocation a) {
    return new AllocationResponse(
        a.getId(), a.getWeek().getId(), a.getType(), a.getLabel(),
        a.getAmount(), a.getPercentage(), a.getCreatedAt(), a.getUpdatedAt());
  }

  private WeekResponse mapWeek(AccountingWeek w) {
    List<AllocationResponse> allocations = w.getAllocations().stream().map(this::mapAllocation).toList();
    return new WeekResponse(
        w.getId(), w.getWeekStart(), w.getWeekEnd(),
        w.getRevenue(), w.getCost(), w.getProfit(), w.getNotes(),
        allocations, w.getCreatedAt(), w.getUpdatedAt());
  }

  private static BigDecimal nz(BigDecimal value) {
    return value == null ? BigDecimal.ZERO : value;
  }

  /** Ganancia: usa la enviada o, si no, ingresos - costos. */
  private static BigDecimal resolveProfit(BigDecimal revenue, BigDecimal cost, BigDecimal profit) {
    if (profit != null) {
      return profit;
    }
    return nz(revenue).subtract(nz(cost));
  }

  // ---------- semanas ----------

  @Transactional(readOnly = true)
  public PageResponse<WeekResponse> listWeeks(Integer pageParam, Integer sizeParam) {
    int page = Paging.page(pageParam);
    int size = Paging.size(sizeParam);
    Page<AccountingWeek> result =
        weekRepo.findAll(Paging.of(page, size, Sort.by(Sort.Direction.DESC, "weekStart")));
    return PageResponse.of(
        result.getContent().stream().map(this::mapWeek).toList(),
        result.getTotalElements(), page, size);
  }

  @Transactional(readOnly = true)
  public WeekResponse getWeek(String id) {
    return mapWeek(findWeek(id));
  }

  private AccountingWeek findWeek(String id) {
    return weekRepo.findById(id).orElseThrow(() -> ApiException.notFound("Semana no encontrada"));
  }

  @Transactional
  public WeekResponse createWeek(CreateWeekRequest req) {
    AccountingWeek w = new AccountingWeek();
    w.setWeekStart(req.weekStart());
    w.setWeekEnd(req.weekEnd());
    w.setRevenue(nz(req.revenue()));
    w.setCost(nz(req.cost()));
    w.setProfit(resolveProfit(req.revenue(), req.cost(), req.profit()));
    w.setNotes(req.notes());
    return mapWeek(weekRepo.save(w));
  }

  @Transactional
  public WeekResponse updateWeek(String id, UpdateWeekRequest req) {
    AccountingWeek w = findWeek(id);
    if (req.weekStart() != null) w.setWeekStart(req.weekStart());
    if (req.weekEnd() != null) w.setWeekEnd(req.weekEnd());
    if (req.revenue() != null) w.setRevenue(req.revenue());
    if (req.cost() != null) w.setCost(req.cost());

    if (req.profit() != null) {
      w.setProfit(req.profit());
    } else if (req.revenue() != null || req.cost() != null) {
      // recalcula si cambiaron ingresos/costos y no se envio profit explicito
      w.setProfit(w.getRevenue().subtract(w.getCost()));
    }

    if (req.notes() != null) w.setNotes(req.notes());
    return mapWeek(weekRepo.save(w));
  }

  @Transactional
  public void removeWeek(String id) {
    if (!weekRepo.existsById(id)) {
      throw ApiException.notFound("Semana no encontrada");
    }
    weekRepo.deleteById(id);
  }

  /** Resumen del reparto: repartido, restante y desglose por tipo. */
  @Transactional(readOnly = true)
  public WeekSummaryResponse summary(String id) {
    AccountingWeek w = findWeek(id);
    BigDecimal profit = nz(w.getProfit());

    Map<String, BigDecimal> byType = new LinkedHashMap<>();
    for (AllocationType t : AllocationType.values()) {
      byType.put(t.name(), BigDecimal.ZERO);
    }
    BigDecimal allocated = BigDecimal.ZERO;
    for (Allocation a : w.getAllocations()) {
      BigDecimal amount = nz(a.getAmount());
      byType.merge(a.getType().name(), amount, BigDecimal::add);
      allocated = allocated.add(amount);
    }

    return new WeekSummaryResponse(
        w.getId(), w.getWeekStart(), w.getWeekEnd(),
        profit, allocated, profit.subtract(allocated),
        byType, w.getAllocations().stream().map(this::mapAllocation).toList());
  }

  // ---------- asignaciones (reparto) ----------

  @Transactional
  public AllocationResponse addAllocation(String weekId, CreateAllocationRequest req) {
    AccountingWeek w = findWeek(weekId);
    Allocation a = new Allocation();
    a.setWeek(w);
    a.setType(req.type());
    a.setLabel(req.label());
    a.setAmount(nz(req.amount()));
    a.setPercentage(req.percentage());
    return mapAllocation(allocationRepo.save(a));
  }

  @Transactional
  public AllocationResponse updateAllocation(String allocationId, UpdateAllocationRequest req) {
    Allocation a = allocationRepo.findById(allocationId)
        .orElseThrow(() -> ApiException.notFound("Asignacion no encontrada"));
    if (req.type() != null) a.setType(req.type());
    if (req.label() != null) a.setLabel(req.label());
    if (req.amount() != null) a.setAmount(req.amount());
    if (req.percentage() != null) a.setPercentage(req.percentage());
    return mapAllocation(allocationRepo.save(a));
  }

  @Transactional
  public void removeAllocation(String allocationId) {
    if (!allocationRepo.existsById(allocationId)) {
      throw ApiException.notFound("Asignacion no encontrada");
    }
    allocationRepo.deleteById(allocationId);
  }
}

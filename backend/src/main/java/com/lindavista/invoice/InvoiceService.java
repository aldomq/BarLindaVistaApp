package com.lindavista.invoice;

import com.lindavista.common.ApiException;
import com.lindavista.common.PageResponse;
import com.lindavista.common.Paging;
import com.lindavista.invoice.InvoiceDtos.CreateInvoiceRequest;
import com.lindavista.invoice.InvoiceDtos.InvoiceResponse;
import com.lindavista.invoice.InvoiceDtos.UpdateInvoiceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class InvoiceService {

  private final InvoiceRepository repo;

  public InvoiceService(InvoiceRepository repo) {
    this.repo = repo;
  }

  private InvoiceResponse map(Invoice i) {
    return new InvoiceResponse(
        i.getId(), i.getInvoiceDate(), i.getVendor(), i.getAmount(), i.getNumber(),
        i.getNotes(), i.getSource(), i.getFileName(), i.getContentType(),
        i.getFileUrl(), i.getPreviewUrl(), i.getAccountingWeekId(),
        i.getCreatedAt(), i.getUpdatedAt());
  }

  /** Lista filtrando por rango de fechas y proveedor, ordenada por fecha (reciente primero). */
  @Transactional(readOnly = true)
  public PageResponse<InvoiceResponse> list(
      LocalDate from, LocalDate to, String vendor, Integer pageParam, Integer sizeParam) {
    int page = Paging.page(pageParam);
    int size = Paging.size(sizeParam);
    String v = (vendor == null || vendor.isBlank()) ? null : vendor.trim();
    Page<Invoice> result = repo.search(
        from, to, v, Paging.of(page, size, Sort.by(Sort.Direction.DESC, "invoiceDate")));
    return PageResponse.of(
        result.getContent().stream().map(this::map).toList(),
        result.getTotalElements(), page, size);
  }

  @Transactional(readOnly = true)
  public InvoiceResponse get(String id) {
    return map(find(id));
  }

  private Invoice find(String id) {
    return repo.findById(id).orElseThrow(() -> ApiException.notFound("Factura no encontrada"));
  }

  @Transactional
  public InvoiceResponse create(CreateInvoiceRequest req, InvoiceSource source) {
    Invoice i = new Invoice();
    i.setInvoiceDate(req.invoiceDate());
    i.setVendor(req.vendor().trim());
    if (req.amount() != null) i.setAmount(req.amount());
    i.setNumber(req.number());
    i.setNotes(req.notes());
    i.setAccountingWeekId(req.accountingWeekId());
    i.setFileName(req.fileName());
    i.setContentType(req.contentType());
    i.setFileUrl(req.fileUrl());
    i.setPreviewUrl(req.previewUrl());
    i.setSource(source);
    return map(repo.save(i));
  }

  @Transactional
  public InvoiceResponse update(String id, UpdateInvoiceRequest req) {
    Invoice i = find(id);
    if (req.invoiceDate() != null) i.setInvoiceDate(req.invoiceDate());
    if (req.vendor() != null) i.setVendor(req.vendor().trim());
    if (req.amount() != null) i.setAmount(req.amount());
    if (req.number() != null) i.setNumber(req.number());
    if (req.notes() != null) i.setNotes(req.notes());
    if (req.accountingWeekId() != null) i.setAccountingWeekId(req.accountingWeekId());
    if (req.fileName() != null) i.setFileName(req.fileName());
    if (req.contentType() != null) i.setContentType(req.contentType());
    if (req.fileUrl() != null) i.setFileUrl(req.fileUrl());
    if (req.previewUrl() != null) i.setPreviewUrl(req.previewUrl());
    return map(repo.save(i));
  }

  @Transactional
  public void delete(String id) {
    if (!repo.existsById(id)) {
      throw ApiException.notFound("Factura no encontrada");
    }
    repo.deleteById(id);
  }
}

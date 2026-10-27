package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Invoice;
import com.example.qlkytucxa.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface InvoiceService extends CrudService<Invoice> {
    Page<Invoice> search(String keyword, InvoiceStatus status, Integer month, Integer year, Pageable pageable);
    Invoice create(Invoice invoice);
}

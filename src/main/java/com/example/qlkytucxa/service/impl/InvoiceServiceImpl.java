package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Invoice;
import com.example.qlkytucxa.entity.InvoiceStatus;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.repository.InvoiceRepository;
import com.example.qlkytucxa.repository.PaymentRepository;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.service.InvoiceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class InvoiceServiceImpl extends AbstractCrudService<Invoice> implements InvoiceService {
    private final InvoiceRepository repository;
    private final PaymentRepository payments;
    public InvoiceServiceImpl(InvoiceRepository repository, PaymentRepository payments) { super(repository, "Hóa đơn"); this.repository = repository; this.payments = payments; }
    @Override public Page<Invoice> search(String keyword, InvoiceStatus status, Integer month, Integer year, Pageable pageable) {
        return repository.searchInvoices(keyword == null || keyword.isBlank() ? null : keyword.trim(), status, month, year, pageable);
    }
    @Override @Transactional public Invoice create(Invoice invoice) {
        if (invoice.getInvoiceCode() == null || invoice.getInvoiceCode().isBlank()) {
            invoice.setInvoiceCode("HDN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        repository.findByInvoiceCode(invoice.getInvoiceCode()).ifPresent(x -> { throw new DuplicateResourceException("Mã hóa đơn đã tồn tại"); });
        if (invoice.getRoomPrice() == null) invoice.setRoomPrice(BigDecimal.ZERO);
        if (invoice.getElectricityAmount() == null) invoice.setElectricityAmount(BigDecimal.ZERO);
        if (invoice.getWaterAmount() == null) invoice.setWaterAmount(BigDecimal.ZERO);
        if (invoice.getServiceAmount() == null) invoice.setServiceAmount(BigDecimal.ZERO);
        if (invoice.getOtherAmount() == null) invoice.setOtherAmount(BigDecimal.ZERO);
        if (invoice.getDiscount() == null) invoice.setDiscount(BigDecimal.ZERO);
        invoice.calculateTotal();
        return repository.save(invoice);
    }

    @Override @Transactional public Invoice save(Invoice invoice) {
        repository.findByInvoiceCode(invoice.getInvoiceCode()).filter(old -> !old.getId().equals(invoice.getId()))
                .ifPresent(old -> { throw new DuplicateResourceException("Mã hóa đơn đã tồn tại"); });
        invoice.calculateTotal();
        return repository.save(invoice);
    }

    @Override @Transactional public void delete(Long id) {
        if (payments.countByInvoiceId(id) > 0) throw new InvalidOperationException("Không thể xóa hóa đơn đã có thanh toán");
        super.delete(id);
    }
}

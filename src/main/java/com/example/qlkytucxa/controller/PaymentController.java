package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.PaymentDto;
import com.example.qlkytucxa.entity.Invoice;
import com.example.qlkytucxa.entity.PaymentMethod;
import com.example.qlkytucxa.entity.Payment;
import com.example.qlkytucxa.entity.PaymentStatus;
import com.example.qlkytucxa.repository.InvoiceRepository;
import com.example.qlkytucxa.repository.PaymentRepository;
import com.example.qlkytucxa.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/payments")
public class PaymentController {
    private final PaymentRepository repository;
    private final InvoiceRepository invoices;
    private final PaymentService service;
    public PaymentController(PaymentRepository repository, InvoiceRepository invoices, PaymentService service) { this.repository = repository; this.invoices = invoices; this.service = service; }
    @GetMapping public String list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) PaymentMethod method,
                                  @RequestParam(required = false) PaymentStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("paymentPage", repository.searchPayments(keyword == null || keyword.isBlank() ? null : keyword.trim(), method, status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("paymentDate").descending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedMethod", method); model.addAttribute("selectedStatus", status);
        model.addAttribute("methods", PaymentMethod.values()); model.addAttribute("statuses", PaymentStatus.values());
        return "payments/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("payment", new PaymentDto()); prepareForm(model); return "payments/form";
    }
    @PostMapping("/pay") public String pay(@Valid @ModelAttribute("payment") PaymentDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepareForm(model); return "payments/form"; }
        Invoice invoice = invoices.findById(dto.getInvoiceId()).orElseThrow(() -> new IllegalArgumentException("Hóa đơn không tồn tại"));
        Payment payment = Payment.builder().invoice(invoice).paymentCode(dto.getPaymentCode()).amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod()).paymentDate(dto.getPaymentDate()).note(dto.getNote()).build();
        service.pay(payment);
        return "redirect:/admin/payments";
    }
    private void prepareForm(Model model) {
        model.addAttribute("invoices", invoices.findByStatus(com.example.qlkytucxa.entity.InvoiceStatus.UNPAID));
        model.addAttribute("methods", PaymentMethod.values());
    }
}

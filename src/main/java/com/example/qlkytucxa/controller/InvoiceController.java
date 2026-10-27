package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.InvoiceDto;
import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.Invoice;
import com.example.qlkytucxa.entity.InvoiceStatus;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.service.InvoiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/invoices")
public class InvoiceController {
    private final InvoiceService service;
    private final ContractRepository contracts;
    public InvoiceController(InvoiceService service, ContractRepository contracts) { this.service = service; this.contracts = contracts; }
    @GetMapping public String list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) InvoiceStatus status,
                                  @RequestParam(required = false) Integer month,
                                  @RequestParam(required = false) Integer year,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("invoicePage", service.search(keyword, status, month, year,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("billingMonth").descending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedStatus", status);
        model.addAttribute("month", month); model.addAttribute("year", year); model.addAttribute("statuses", InvoiceStatus.values());
        return "invoices/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("invoice", new InvoiceDto()); prepareForm(model); return "invoices/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Invoice i = service.getById(id);
        model.addAttribute("invoice", InvoiceDto.builder().id(i.getId()).contractId(i.getContract().getId()).invoiceCode(i.getInvoiceCode())
                .billingMonth(i.getBillingMonth()).roomPrice(i.getRoomPrice()).electricityAmount(i.getElectricityAmount()).waterAmount(i.getWaterAmount())
                .serviceAmount(i.getServiceAmount()).otherAmount(i.getOtherAmount()).discount(i.getDiscount()).totalAmount(i.getTotalAmount())
                .dueDate(i.getDueDate()).status(i.getStatus()).build());
        prepareForm(model); return "invoices/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("invoice") InvoiceDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepareForm(model); return "invoices/form"; }
        Contract contract = contracts.findById(dto.getContractId()).orElseThrow(() -> new IllegalArgumentException("Hợp đồng không tồn tại"));
        Invoice entity = Invoice.builder().id(dto.getId()).contract(contract).invoiceCode(dto.getInvoiceCode()).billingMonth(dto.getBillingMonth())
                .roomPrice(dto.getRoomPrice()).electricityAmount(dto.getElectricityAmount()).waterAmount(dto.getWaterAmount())
                .serviceAmount(dto.getServiceAmount()).otherAmount(dto.getOtherAmount()).discount(dto.getDiscount()).dueDate(dto.getDueDate())
                .status(dto.getStatus() == null ? InvoiceStatus.UNPAID : dto.getStatus()).build();
        if (dto.getId() == null) service.create(entity); else service.save(entity);
        return "redirect:/admin/invoices";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/admin/invoices"; }
    private void prepareForm(Model model) { model.addAttribute("contracts", contracts.findAll()); model.addAttribute("statuses", InvoiceStatus.values()); }
}

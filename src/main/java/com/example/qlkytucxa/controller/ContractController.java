package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.ContractDto;
import com.example.qlkytucxa.entity.Bed;
import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.ContractStatus;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.InvoiceRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.service.ContractService;
import com.example.qlkytucxa.service.RentalRequestService;
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
@RequestMapping("/admin/contracts")
public class ContractController {
    private final ContractService service;
    private final TenantRepository tenants;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final RentalRequestService rentalRequests;
    private final InvoiceRepository invoices;
    public ContractController(ContractService service, TenantRepository tenants, RoomRepository rooms, BedRepository beds,
                              RentalRequestService rentalRequests, InvoiceRepository invoices) { this.service = service; this.tenants = tenants; this.rooms = rooms; this.beds = beds; this.rentalRequests = rentalRequests; this.invoices = invoices; }
    @GetMapping public String list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) ContractStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("contractPage", service.search(keyword, status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("startDate").descending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", ContractStatus.values());
        model.addAttribute("pendingInvoiceContractIds", java.util.Set.copyOf(invoices.findPendingRentalContractIdsWithUnpaidInvoices()));
        return "contracts/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("contract", ContractDto.builder().status(ContractStatus.PENDING).build());
        prepareForm(model); return "contracts/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Contract c = service.getById(id);
        model.addAttribute("contract", ContractDto.builder().id(c.getId()).tenantId(c.getTenant().getId()).roomId(c.getRoom().getId())
                .bedId(c.getBed() == null ? null : c.getBed().getId()).contractCode(c.getContractCode()).startDate(c.getStartDate())
                .endDate(c.getEndDate()).monthlyPrice(c.getMonthlyPrice()).deposit(c.getDeposit()).status(c.getStatus()).note(c.getNote()).build());
        prepareForm(model); return "contracts/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("contract") ContractDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepareForm(model); return "contracts/form"; }
        Tenant tenant = tenants.findById(dto.getTenantId()).orElseThrow(() -> new IllegalArgumentException("Người thuê không tồn tại"));
        Room room = rooms.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        Bed bed = dto.getBedId() == null ? null : beds.findById(dto.getBedId()).orElseThrow(() -> new IllegalArgumentException("Giường không tồn tại"));
        Contract entity = Contract.builder().id(dto.getId()).tenant(tenant).room(room).bed(bed).contractCode(dto.getContractCode())
                .startDate(dto.getStartDate()).endDate(dto.getEndDate()).monthlyPrice(dto.getMonthlyPrice()).deposit(dto.getDeposit())
                .status(dto.getStatus() == null ? ContractStatus.PENDING : dto.getStatus()).note(dto.getNote()).build();
        if (dto.getId() == null) service.create(entity); else service.update(dto.getId(), entity);
        return "redirect:/admin/contracts";
    }
    @PostMapping("/{id}/finish") public String finish(@PathVariable Long id) { service.finish(id); return "redirect:/admin/contracts"; }
    @PostMapping("/{id}/approve") public String approveRentalRequest(@PathVariable Long id) {
        rentalRequests.approve(id);
        return "redirect:/admin/contracts?approved";
    }
    @PostMapping("/{id}/reject") public String rejectRentalRequest(@PathVariable Long id) {
        rentalRequests.reject(id);
        return "redirect:/admin/contracts?rejected";
    }
    private void prepareForm(Model model) {
        model.addAttribute("tenants", tenants.findAll()); model.addAttribute("rooms", rooms.findAll()); model.addAttribute("beds", beds.findAll());
        model.addAttribute("statuses", ContractStatus.values());
    }
}

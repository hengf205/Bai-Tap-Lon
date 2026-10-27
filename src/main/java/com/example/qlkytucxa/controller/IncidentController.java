package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.IncidentDto;
import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.ContractStatus;
import com.example.qlkytucxa.entity.Incident;
import com.example.qlkytucxa.entity.IncidentPriority;
import com.example.qlkytucxa.entity.IncidentStatus;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.User;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;

@Controller
public class IncidentController {
    private final IncidentService service;
    private final RoomRepository rooms;
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    public IncidentController(IncidentService service, RoomRepository rooms, TenantRepository tenants, ContractRepository contracts) { this.service = service; this.rooms = rooms; this.tenants = tenants; this.contracts = contracts; }

    @GetMapping({"/admin/incidents", "/manager/incidents"})
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) IncidentStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("incidentPage", service.search(keyword, status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("createdAt").descending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", IncidentStatus.values());
        return "incidents/list";
    }

    @PostMapping({"/admin/incidents/{id}/status", "/manager/incidents/{id}/status"})
    public String updateStatus(@PathVariable Long id, @RequestParam IncidentStatus status) {
        service.changeStatus(id, status);
        return "redirect:/manager/incidents";
    }

    @GetMapping({"/admin/incidents/new", "/manager/incidents/new"})
    public String createForm(Model model) {
        model.addAttribute("incident", IncidentDto.builder().priority(IncidentPriority.MEDIUM).build());
        model.addAttribute("rooms", rooms.findAll()); model.addAttribute("priorities", IncidentPriority.values());
        return "incidents/form";
    }

    @GetMapping("/tenant/incidents/new")
    public String tenantForm(@AuthenticationPrincipal User user, Model model) {
        Tenant tenant = tenants.findByUsername(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Hồ sơ người thuê", -1L));
        Contract active = contracts.findByTenantIdAndStatus(tenant.getId(), ContractStatus.ACTIVE).orElse(null);
        model.addAttribute("incident", IncidentDto.builder().tenantId(tenant.getId()).roomId(active == null ? null : active.getRoom().getId()).priority(IncidentPriority.MEDIUM).build());
        model.addAttribute("rooms", active == null ? java.util.List.of() : java.util.List.of(active.getRoom()));
        model.addAttribute("priorities", IncidentPriority.values()); model.addAttribute("tenantReport", true);
        return "incidents/form";
    }

    @PostMapping("/admin/incidents/save")
    public String saveAdmin(@Valid @ModelAttribute("incident") IncidentDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("rooms", rooms.findAll()); model.addAttribute("priorities", IncidentPriority.values()); return "incidents/form"; }
        saveIncident(dto, null);
        return "redirect:/admin/incidents";
    }

    @PostMapping("/manager/incidents/save")
    public String saveManager(@Valid @ModelAttribute("incident") IncidentDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("rooms", rooms.findAll()); model.addAttribute("priorities", IncidentPriority.values()); return "incidents/form"; }
        saveIncident(dto, null);
        return "redirect:/manager/incidents";
    }

    @PostMapping("/tenant/incidents/save")
    public String saveTenant(@Valid @ModelAttribute("incident") IncidentDto dto, BindingResult result,
                             @AuthenticationPrincipal User user, Model model) {
        Tenant tenant = tenants.findByUsername(user.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Hồ sơ người thuê", -1L));
        Contract active = contracts.findByTenantIdAndStatus(tenant.getId(), ContractStatus.ACTIVE).orElse(null);
        if (active == null || dto.getRoomId() == null || !active.getRoom().getId().equals(dto.getRoomId())) {
            result.rejectValue("roomId", "invalid.room", "Bạn chỉ có thể báo sự cố cho phòng đang thuê");
        }
        if (result.hasErrors()) {
            model.addAttribute("rooms", active == null ? java.util.List.of() : java.util.List.of(active.getRoom()));
            model.addAttribute("priorities", IncidentPriority.values()); model.addAttribute("tenantReport", true);
            return "incidents/form";
        }
        saveIncident(dto, tenant);
        return "redirect:/tenant/incidents";
    }

    private void saveIncident(IncidentDto dto, Tenant tenant) {
        Room room = rooms.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        Incident entity = Incident.builder().room(room).tenant(tenant).title(dto.getTitle().trim()).description(dto.getDescription())
                .image(dto.getImage()).priority(dto.getPriority()).status(IncidentStatus.PENDING).build();
        service.save(entity);
    }
}

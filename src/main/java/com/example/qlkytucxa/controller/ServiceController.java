package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.ServiceDto;
import com.example.qlkytucxa.entity.Service;
import com.example.qlkytucxa.entity.ServiceStatus;
import com.example.qlkytucxa.service.ServiceCatalogService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/services")
public class ServiceController {
    private final ServiceCatalogService service;
    public ServiceController(ServiceCatalogService service) { this.service = service; }
    @GetMapping public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("servicePage", service.getAll(PageRequest.of(Math.max(page, 0), 15, Sort.by("serviceName").ascending())));
        return "services/list";
    }
    @GetMapping("/new") public String createForm(Model model) {
        ServiceDto dto = new ServiceDto(); dto.setStatus(ServiceStatus.ACTIVE); dto.setPrice(java.math.BigDecimal.ZERO);
        model.addAttribute("service", dto); prepare(model); return "services/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Service s = service.getById(id); ServiceDto dto = new ServiceDto(); dto.setId(s.getId()); dto.setServiceName(s.getServiceName());
        dto.setUnit(s.getUnit()); dto.setPrice(s.getPrice()); dto.setStatus(s.getStatus()); dto.setDescription(s.getDescription());
        model.addAttribute("service", dto); prepare(model); return "services/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("service") ServiceDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepare(model); return "services/form"; }
        Service entity = Service.builder().id(dto.getId()).serviceName(dto.getServiceName().trim()).unit(dto.getUnit())
                .price(dto.getPrice()).status(dto.getStatus()).description(dto.getDescription()).build();
        service.save(entity); return "redirect:/admin/services";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/admin/services"; }
    private void prepare(Model model) { model.addAttribute("statuses", ServiceStatus.values()); }
}

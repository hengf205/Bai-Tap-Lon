package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.TenantDto;
import com.example.qlkytucxa.entity.Gender;
import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.TenantStatus;
import com.example.qlkytucxa.entity.User;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.repository.UserRepository;
import com.example.qlkytucxa.service.TenantService;
import com.example.qlkytucxa.service.CampusSettingsService;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tenants")
public class TenantController {
    private final TenantService service;
    private final TenantRepository tenants;
    private final UserRepository users;
    private final CampusSettingsService campusSettings;
    public TenantController(TenantService service, TenantRepository tenants, UserRepository users, CampusSettingsService campusSettings) { this.service = service; this.tenants = tenants; this.users = users; this.campusSettings = campusSettings; }
    @GetMapping public String list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) TenantStatus status,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("tenantPage", service.search(keyword, status,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("fullName").ascending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", TenantStatus.values());
        return "tenants/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("tenant", new TenantDto()); model.addAttribute("genders", Gender.values());
        model.addAttribute("statuses", TenantStatus.values()); model.addAttribute("userAccounts", users.findAll()); return "tenants/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Tenant t = tenants.findByIdWithUser(id).orElseThrow(() -> new ResourceNotFoundException("Người thuê", id));
        TenantDto dto = TenantDto.builder().id(t.getId()).userId(t.getUser() == null ? null : t.getUser().getId())
                .studentCode(t.getStudentCode()).fullName(t.getFullName()).dateOfBirth(t.getDateOfBirth()).gender(t.getGender())
                .identityCard(t.getIdentityCard()).phone(t.getPhone()).email(t.getEmail()).hometown(t.getHometown()).school(t.getSchool())
                .major(t.getMajor()).avatar(t.getAvatar()).status(t.getStatus()).build();
        model.addAttribute("tenant", dto); model.addAttribute("genders", Gender.values());
        model.addAttribute("statuses", TenantStatus.values()); model.addAttribute("userAccounts", users.findAll()); return "tenants/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("tenant") TenantDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("genders", Gender.values()); model.addAttribute("statuses", TenantStatus.values()); model.addAttribute("userAccounts", users.findAll()); return "tenants/form"; }
        User user = dto.getUserId() == null ? null : users.findById(dto.getUserId()).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));
        Tenant entity = Tenant.builder().id(dto.getId()).user(user).studentCode(dto.getStudentCode()).fullName(dto.getFullName().trim())
                .dateOfBirth(dto.getDateOfBirth()).gender(dto.getGender()).identityCard(dto.getIdentityCard()).phone(dto.getPhone())
                .email(dto.getEmail()).hometown(dto.getHometown()).school(campusSettings.getSettings().getInstitutionName()).major(dto.getMajor()).avatar(dto.getAvatar())
                .status(dto.getStatus() == null ? TenantStatus.ACTIVE : dto.getStatus()).build();
        service.save(entity); return "redirect:/admin/tenants";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            service.delete(id);
            redirect.addFlashAttribute("successMessage", "Đã ngừng hồ sơ người thuê. Lịch sử hợp đồng và sự cố vẫn được lưu.");
        } catch (InvalidOperationException exception) {
            redirect.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/admin/tenants";
    }
}

package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.UserDto;
import com.example.qlkytucxa.entity.Role;
import com.example.qlkytucxa.entity.User;
import com.example.qlkytucxa.entity.UserStatus;
import com.example.qlkytucxa.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class UserController {
    private final UserService service;
    public UserController(UserService service) { this.service = service; }
    @GetMapping public String list(@RequestParam(required = false) String keyword, @RequestParam(required = false) Role role,
                                  @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("userPage", service.search(keyword, role, PageRequest.of(Math.max(page, 0), 15, Sort.by("username").ascending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedRole", role); model.addAttribute("roles", Role.values());
        return "users/list";
    }
    @GetMapping("/new") public String createForm(Model model) {
        UserDto dto = new UserDto(); dto.setStatus(UserStatus.ACTIVE); model.addAttribute("user", dto); prepare(model); return "users/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        User u = service.getById(id); UserDto dto = new UserDto(); dto.setId(u.getId()); dto.setUsername(u.getUsername());
        dto.setFullName(u.getFullName()); dto.setEmail(u.getEmail()); dto.setPhone(u.getPhone()); dto.setRole(u.getRole()); dto.setStatus(u.getStatus());
        model.addAttribute("user", dto); prepare(model); return "users/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("user") UserDto dto, BindingResult result, Model model) {
        if (dto.getId() == null && (dto.getPassword() == null || dto.getPassword().isBlank())) result.rejectValue("password", "required", "Mật khẩu là bắt buộc khi tạo tài khoản");
        if (result.hasErrors()) { prepare(model); return "users/form"; }
        User user = User.builder().id(dto.getId()).username(dto.getUsername().trim()).fullName(dto.getFullName().trim())
                .email(dto.getEmail()).phone(dto.getPhone()).role(dto.getRole()).status(dto.getStatus() == null ? UserStatus.ACTIVE : dto.getStatus()).build();
        service.save(user, dto.getPassword()); return "redirect:/admin/users";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/admin/users"; }
    private void prepare(Model model) { model.addAttribute("roles", Role.values()); model.addAttribute("statuses", UserStatus.values()); }
}

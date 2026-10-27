package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.ChangePasswordDto;
import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.*;
import com.example.qlkytucxa.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/tenant")
public class TenantPortalController {
    private final TenantRepository tenants;
    private final ContractRepository contracts;
    private final InvoiceRepository invoices;
    private final PaymentRepository payments;
    private final IncidentRepository incidents;
    private final BedRepository beds;
    private final NotificationRepository notifications;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final PaymentService paymentService;

    public TenantPortalController(TenantRepository tenants, ContractRepository contracts, InvoiceRepository invoices,
                                  PaymentRepository payments, IncidentRepository incidents, BedRepository beds,
                                  NotificationRepository notifications, UserRepository users, PasswordEncoder passwordEncoder,
                                  PaymentService paymentService) {
        this.tenants = tenants; this.contracts = contracts; this.invoices = invoices; this.payments = payments;
        this.incidents = incidents; this.beds = beds; this.notifications = notifications; this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.paymentService = paymentService;
    }

    @GetMapping("/room")
    public String room(@AuthenticationPrincipal User user, Model model) {
        Tenant tenant = getTenant(user);
        Contract active = contracts.findByTenantIdAndStatus(tenant.getId(), ContractStatus.ACTIVE).orElse(null);
        model.addAttribute("tenant", tenant);
        model.addAttribute("contract", active);
        model.addAttribute("beds", active == null ? java.util.List.of() : beds.findByRoomId(active.getRoom().getId()));
        return "tenant/room";
    }

    @GetMapping("/contracts")
    public String contracts(@AuthenticationPrincipal User user, Model model) {
        Tenant tenant = getTenant(user);
        model.addAttribute("contracts", contracts.findByTenantIdOrderByStartDateDesc(tenant.getId()));
        return "tenant/contracts";
    }

    @GetMapping("/invoices")
    public String invoices(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("invoices", invoices.findByTenantUsername(user.getUsername()));
        return "tenant/invoices";
    }

    @PostMapping("/invoices/{id}/pay")
    public String payInvoice(@PathVariable Long id, @RequestParam PaymentMethod paymentMethod,
                             @AuthenticationPrincipal User user) {
        Invoice invoice = invoices.findByIdAndTenantUsername(id, user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Hóa đơn", id));
        Payment payment = Payment.builder().invoice(invoice).amount(invoice.getTotalAmount())
                .paymentMethod(paymentMethod).note("Người thuê xác nhận thanh toán qua cổng nội bộ.").build();
        paymentService.pay(payment);
        return "redirect:/tenant/invoices?paymentSuccess";
    }

    @GetMapping("/payments")
    public String payments(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("payments", payments.findByTenantUsername(user.getUsername()));
        return "tenant/payments";
    }

    @GetMapping("/incidents")
    public String incidents(@AuthenticationPrincipal User user, Model model) {
        Tenant tenant = getTenant(user);
        model.addAttribute("incidents", incidents.findByTenantIdOrderByCreatedAtDesc(tenant.getId()));
        return "tenant/incidents";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("tenant", getTenant(user));
        return "tenant/profile";
    }

    @GetMapping("/notifications")
    public String notifications(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("notifications", notifications.findByUserIdOrderByCreatedAtDesc(user.getId()));
        return "tenant/notifications";
    }

    @PostMapping("/notifications/{id}/read")
    public String markNotificationRead(@PathVariable Long id, @AuthenticationPrincipal User user) {
        notifications.markAsReadForUser(id, user.getId());
        return "redirect:/tenant/notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllNotificationsRead(@AuthenticationPrincipal User user) {
        notifications.markAllAsRead(user.getId());
        return "redirect:/tenant/notifications";
    }

    @GetMapping("/password")
    public String passwordForm(Model model) {
        model.addAttribute("changePassword", new ChangePasswordDto());
        return "tenant/password";
    }

    @PostMapping("/password")
    public String changePassword(@Valid @ModelAttribute("changePassword") ChangePasswordDto dto,
                                 BindingResult result, @AuthenticationPrincipal User principal, Model model) {
        if (!result.hasErrors() && !passwordEncoder.matches(dto.getCurrentPassword(), principal.getPassword())) {
            result.rejectValue("currentPassword", "invalid.currentPassword", "Mật khẩu hiện tại không đúng");
        }
        if (result.hasErrors()) return "tenant/password";
        User user = users.findById(principal.getId()).orElseThrow(() -> new ResourceNotFoundException("Tài khoản", principal.getId()));
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        users.save(user);
        model.addAttribute("passwordChanged", true);
        model.addAttribute("changePassword", new ChangePasswordDto());
        return "tenant/password";
    }

    private Tenant getTenant(User user) {
        return tenants.findByUsername(user.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ người thuê", user.getId()));
    }
}

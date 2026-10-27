package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity người thuê / sinh viên.
 * Liên kết 1-1 với User account.
 */
@Entity
@Table(name = "tenants")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * OneToOne: Một tenant tương ứng một tài khoản user.
     * user_id là FK trong bảng tenants.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;

    @Column(name = "student_code", unique = true, length = 20)
    private String studentCode;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "identity_card", unique = true, length = 20)
    private String identityCard;

    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    @Column(name = "phone", length = 15)
    private String phone;

    @Email(message = "Email không đúng định dạng")
    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "hometown", length = 200)
    private String hometown;

    @Column(name = "school", length = 150)
    private String school;

    @Column(name = "major", length = 100)
    private String major;

    @Column(name = "avatar", length = 255)
    private String avatar;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TenantStatus status = TenantStatus.ACTIVE;

    // ========================
    // Relationships
    // ========================

    /**
     * Một người thuê có thể có nhiều hợp đồng (theo thời gian).
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contract> contracts;

    /**
     * Sự cố do người thuê báo.
     */
    @OneToMany(mappedBy = "tenant", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Incident> incidents;

    /**
     * Lấy hợp đồng đang hoạt động (ACTIVE).
     */
    @Transient
    public Contract getActiveContract() {
        if (contracts == null) return null;
        return contracts.stream()
                .filter(c -> c.getStatus() == ContractStatus.ACTIVE)
                .findFirst()
                .orElse(null);
    }
}

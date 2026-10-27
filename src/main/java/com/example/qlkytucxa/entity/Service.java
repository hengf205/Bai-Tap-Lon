package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity dịch vụ (internet, vệ sinh, bảo vệ, v.v.).
 */
@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên dịch vụ không được để trống")
    @Column(name = "service_name", nullable = false, length = 100)
    private String serviceName;

    @Column(name = "unit", length = 50)
    private String unit;  // Đơn vị: tháng, lần, người, v.v.

    @NotNull(message = "Giá dịch vụ không được để trống")
    @DecimalMin(value = "0.0", message = "Giá dịch vụ không được âm")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ServiceStatus status = ServiceStatus.ACTIVE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ========================
    // Relationships
    // ========================

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomService> roomServices;
}

package com.example.qlkytucxa.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Dashboard dành cho Tenant (người thuê).
 * Hiển thị thông tin tổng hợp trên trang chủ của người thuê.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDashboardDto {

    // Thông tin cá nhân
    private String fullName;
    private String studentCode;
    private String avatar;

    // Phòng đang thuê
    private String roomCode;
    private String buildingName;
    private String floorInfo;

    // Hợp đồng
    private String contractCode;
    private LocalDate contractEndDate;
    private String contractStatus;
    private BigDecimal monthlyPrice;

    // Hóa đơn tháng này
    private String currentInvoiceCode;
    private BigDecimal currentInvoiceTotal;
    private LocalDate dueDate;
    private String invoiceStatus;
    private boolean isOverdue;

    // Sự cố đang xử lý
    private long pendingIncidentsCount;

    // Thông báo chưa đọc
    private long unreadNotifications;
}

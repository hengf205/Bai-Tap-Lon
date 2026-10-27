package com.example.qlkytucxa.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO dành riêng cho Dashboard Admin.
 * Gom tất cả số liệu thống kê vào một object để truyền vào view.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardDto {

    // ======= Thống kê tổng quan =======
    private long totalBuildings;
    private long totalRooms;
    private long availableRooms;
    private long occupiedRooms;
    private long maintenanceRooms;
    private long totalTenants;
    private long activeTenants;
    private long unpaidInvoices;
    private long overdueInvoices;
    private long pendingIncidents;
    private long activeContracts;

    // ======= Doanh thu =======
    private BigDecimal currentMonthRevenue;
    private BigDecimal currentMonthUtility;

    // ======= Dữ liệu cho biểu đồ Chart.js =======

    /** Doanh thu 12 tháng trong năm: [tháng 1, tháng 2, ..., tháng 12] */
    private List<BigDecimal> monthlyRevenueData;

    /** Doanh thu tiền phòng theo tháng trong năm. */
    private List<BigDecimal> roomRevenueData;

    /** Doanh thu dịch vụ theo tháng trong năm. */
    private List<BigDecimal> serviceRevenueData;

    /** Nhãn tháng: ["T1", "T2", ..., "T12"] */
    private List<String> monthLabels;

    /**
     * Số người thuê theo từng tòa nhà.
     * Map key = tên tòa nhà, value = số người.
     */
    private Map<String, Long> tenantsByBuilding;

    /** Tỷ lệ phòng: [trống, đang ở, đầy, bảo trì] */
    private List<Long> roomStatusData;
    private List<String> roomStatusLabels;

    // ======= Năm đang xem thống kê =======
    private int currentYear;
    private int currentMonth;
}

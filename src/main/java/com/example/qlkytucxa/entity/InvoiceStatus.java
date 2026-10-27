package com.example.qlkytucxa.entity;

/**
 * Enum trạng thái hóa đơn.
 */
public enum InvoiceStatus {
    UNPAID,    // Chưa thanh toán
    PAID,      // Đã thanh toán
    OVERDUE,   // Quá hạn
    CANCELLED  // Đã hủy
}

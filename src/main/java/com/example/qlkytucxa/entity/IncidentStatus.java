package com.example.qlkytucxa.entity;

/**
 * Enum trạng thái sự cố/báo hỏng.
 */
public enum IncidentStatus {
    PENDING,    // Chờ xử lý
    PROCESSING, // Đang xử lý
    RESOLVED,   // Đã giải quyết
    CANCELLED   // Đã hủy
}

package com.example.qlkytucxa.entity;

/**
 * Enum trạng thái thanh toán.
 */
public enum PaymentStatus {
    SUCCESS,  // Thành công
    PENDING,  // Đang xử lý
    FAILED,   // Thất bại
    REFUNDED  // Đã hoàn tiền
}

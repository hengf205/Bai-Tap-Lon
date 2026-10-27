package com.example.qlkytucxa.util;

import org.springframework.stereotype.Component;

/** Vietnamese labels for enum values rendered in Thymeleaf views. */
@Component("displayLabels")
public class DisplayLabels {

    public String label(Object value) {
        if (value == null) return "—";
        if (!(value instanceof Enum<?> enumValue)) return value.toString();
        return label(enumValue.getDeclaringClass().getSimpleName(), enumValue.name());
    }

    public String label(String enumType, String value) {
        if (enumType == null || value == null) return "—";
        String key = enumType + "." + value;
        return switch (key) {
            case "Role.ROLE_ADMIN" -> "Quản trị viên";
            case "Role.ROLE_MANAGER" -> "Quản lý";
            case "Role.ROLE_TENANT" -> "Người thuê";
            case "RoomStatus.AVAILABLE", "BedStatus.AVAILABLE" -> "Còn trống";
            case "RoomStatus.OCCUPIED", "BedStatus.OCCUPIED" -> "Đang có người ở";
            case "RoomStatus.FULL" -> "Đã đủ người";
            case "RoomStatus.MAINTENANCE", "BuildingStatus.MAINTENANCE" -> "Đang bảo trì";
            case "BedStatus.RESERVED" -> "Đã đặt trước";
            case "BedStatus.BROKEN" -> "Bị hỏng";
            case "ContractStatus.PENDING", "TenantStatus.PENDING" -> "Chờ xác nhận";
            case "ContractStatus.ACTIVE" -> "Đang hiệu lực";
            case "ContractStatus.EXPIRED" -> "Đã hết hạn";
            case "ContractStatus.CANCELLED" -> "Đã hủy";
            case "TenantStatus.ACTIVE" -> "Đang thuê";
            case "TenantStatus.INACTIVE", "UserStatus.INACTIVE", "BuildingStatus.INACTIVE", "ServiceStatus.INACTIVE" -> "Ngừng hoạt động";
            case "UserStatus.ACTIVE", "BuildingStatus.ACTIVE", "ServiceStatus.ACTIVE" -> "Đang hoạt động";
            case "UserStatus.BANNED" -> "Đã khóa";
            case "InvoiceStatus.UNPAID" -> "Chưa thanh toán";
            case "InvoiceStatus.PAID" -> "Đã thanh toán";
            case "PaymentStatus.SUCCESS" -> "Thành công";
            case "InvoiceStatus.OVERDUE" -> "Quá hạn";
            case "InvoiceStatus.CANCELLED" -> "Đã hủy";
            case "PaymentStatus.PENDING" -> "Đang xử lý";
            case "PaymentStatus.FAILED" -> "Thất bại";
            case "PaymentStatus.REFUNDED" -> "Đã hoàn tiền";
            case "PaymentMethod.CASH" -> "Tiền mặt";
            case "PaymentMethod.BANK_TRANSFER" -> "Chuyển khoản";
            case "PaymentMethod.QR_CODE" -> "Mã QR";
            case "IncidentPriority.LOW" -> "Thấp";
            case "IncidentPriority.MEDIUM" -> "Trung bình";
            case "IncidentPriority.HIGH" -> "Cao";
            case "IncidentPriority.URGENT" -> "Khẩn cấp";
            case "IncidentStatus.PENDING" -> "Chờ xử lý";
            case "IncidentStatus.PROCESSING" -> "Đang xử lý";
            case "IncidentStatus.RESOLVED" -> "Đã giải quyết";
            case "IncidentStatus.CANCELLED" -> "Đã hủy";
            case "RoomType.SINGLE" -> "Phòng đơn";
            case "RoomType.DOUBLE" -> "Phòng đôi";
            case "RoomType.TRIPLE" -> "Phòng ba người";
            case "RoomType.QUAD" -> "Phòng bốn người";
            case "RoomType.LARGE" -> "Phòng lớn";
            case "Gender.MALE" -> "Nam";
            case "Gender.FEMALE" -> "Nữ";
            case "Gender.OTHER" -> "Khác";
            case "AssetCondition.GOOD" -> "Tốt";
            case "AssetCondition.FAIR" -> "Trung bình";
            case "AssetCondition.POOR" -> "Kém";
            case "AssetCondition.BROKEN" -> "Bị hỏng";
            case "NotificationType.INVOICE" -> "Hóa đơn";
            case "NotificationType.CONTRACT" -> "Hợp đồng";
            case "NotificationType.INCIDENT" -> "Sự cố";
            case "NotificationType.PAYMENT" -> "Thanh toán";
            case "NotificationType.SYSTEM" -> "Hệ thống";
            case "NotificationType.GENERAL" -> "Thông báo chung";
            default -> value.replace('_', ' ');
        };
    }
}

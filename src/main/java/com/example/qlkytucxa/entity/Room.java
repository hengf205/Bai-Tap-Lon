package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity phòng ở trong ký túc xá.
 * Liên kết với tầng, giường, hợp đồng, dịch vụ, tài sản và sự cố.
 */
@Entity
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Nhiều phòng thuộc một tầng.
     */
    @NotNull(message = "Tầng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Floor floor;

    @NotBlank(message = "Mã phòng không được để trống")
    @Column(name = "room_code", unique = true, nullable = false, length = 20)
    private String roomCode;

    @NotNull(message = "Loại phòng không được để trống")
    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false)
    private RoomType roomType;

    @NotNull(message = "Sức chứa không được để trống")
    @Min(value = 1, message = "Sức chứa phải lớn hơn 0")
    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @NotNull(message = "Giá phòng không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phòng phải lớn hơn 0")
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.AVAILABLE;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ========================
    // Relationships
    // ========================

    /**
     * Một phòng có nhiều giường.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Bed> beds;

    /**
     * Một phòng có nhiều hợp đồng (qua các thời điểm).
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contract> contracts;

    /**
     * Một phòng có nhiều bản ghi điện nước.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<UtilityMeter> utilityMeters;

    /**
     * Dịch vụ của phòng thông qua bảng trung gian room_services.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<RoomService> roomServices;

    /**
     * Tài sản trong phòng.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Asset> assets;

    /**
     * Sự cố trong phòng.
     */
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Incident> incidents;

    /**
     * Phương thức tiện ích: lấy số giường còn trống.
     */
    @Transient
    public long getAvailableBedCount() {
        if (beds == null) return 0;
        return beds.stream()
                .filter(b -> b.getStatus() == BedStatus.AVAILABLE)
                .count();
    }

    /**
     * Lấy tên tòa nhà từ tầng (tiện ích cho hiển thị).
     */
    @Transient
    public String getBuildingName() {
        if (floor != null && floor.getBuilding() != null) {
            return floor.getBuilding().getBuildingName();
        }
        return "";
    }
}

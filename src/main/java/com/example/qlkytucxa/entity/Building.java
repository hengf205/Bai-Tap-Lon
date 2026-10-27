package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Entity tòa nhà trong khu ký túc xá.
 */
@Entity
@Table(name = "buildings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Mã tòa nhà không được để trống")
    @Column(name = "building_code", unique = true, nullable = false, length = 20)
    private String buildingCode;

    @NotBlank(message = "Tên tòa nhà không được để trống")
    @Column(name = "building_name", nullable = false, length = 100)
    private String buildingName;

    @Column(name = "address", length = 255)
    private String address;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    @Column(name = "number_of_floors")
    private Integer numberOfFloors;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BuildingStatus status = BuildingStatus.ACTIVE;

    // ========================
    // Relationships
    // ========================

    /**
     * Một tòa nhà có nhiều tầng.
     * Khi xóa tòa nhà, xóa toàn bộ tầng liên quan.
     */
    @OneToMany(mappedBy = "building", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Floor> floors;

    /**
     * Tính số phòng trong tòa nhà (transient - không lưu vào DB).
     */
    @Transient
    public int getTotalRooms() {
        if (floors == null) return 0;
        return floors.stream()
                .filter(f -> f.getRooms() != null)
                .mapToInt(f -> f.getRooms().size())
                .sum();
    }
}

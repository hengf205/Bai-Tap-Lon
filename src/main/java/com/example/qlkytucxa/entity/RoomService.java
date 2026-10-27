package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Entity bảng trung gian: dịch vụ được đăng ký cho từng phòng.
 * Thay thế @ManyToMany để có thể thêm trường quantity.
 */
@Entity
@Table(name = "room_services")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Phòng đăng ký dịch vụ.
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    /**
     * ManyToOne: Dịch vụ được chọn.
     */
    @NotNull(message = "Dịch vụ không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Service service;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Column(name = "quantity")
    @Builder.Default
    private Integer quantity = 1;
}

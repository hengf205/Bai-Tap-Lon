package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity tài sản trong phòng (giường, bàn, tủ, điều hòa, v.v.).
 */
@Entity
@Table(name = "assets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Nhiều tài sản thuộc một phòng.
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    @NotBlank(message = "Tên tài sản không được để trống")
    @Column(name = "asset_name", nullable = false, length = 100)
    private String assetName;

    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "condition_status")
    @Builder.Default
    private AssetCondition conditionStatus = AssetCondition.GOOD;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;
}

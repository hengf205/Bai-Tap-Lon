package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Entity tầng trong tòa nhà.
 */
@Entity
@Table(name = "floors", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"building_id", "floor_number"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Floor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Mối quan hệ ManyToOne: Nhiều tầng thuộc một tòa nhà.
     * FK: building_id trong bảng floors.
     */
    @NotNull(message = "Tòa nhà không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Building building;

    @NotNull(message = "Số tầng không được để trống")
    @Min(value = 1, message = "Số tầng phải lớn hơn 0")
    @Column(name = "floor_number", nullable = false)
    private Integer floorNumber;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // ========================
    // Relationships
    // ========================

    /**
     * Một tầng có nhiều phòng.
     */
    @OneToMany(mappedBy = "floor", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Room> rooms;
}

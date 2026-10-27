package com.example.qlkytucxa.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

/**
 * Entity giường trong phòng.
 */
@Entity
@Table(name = "beds")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne: Nhiều giường thuộc một phòng.
     */
    @NotNull(message = "Phòng không được để trống")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Room room;

    @NotBlank(message = "Mã giường không được để trống")
    @Column(name = "bed_code", unique = true, nullable = false, length = 20)
    private String bedCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BedStatus status = BedStatus.AVAILABLE;

    // ========================
    // Relationships
    // ========================

    /**
     * Danh sách hợp đồng liên quan đến giường này.
     */
    @OneToMany(mappedBy = "bed", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Contract> contracts;
}

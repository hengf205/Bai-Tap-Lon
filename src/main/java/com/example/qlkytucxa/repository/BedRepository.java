package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Bed;
import com.example.qlkytucxa.entity.BedStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý giường trong phòng.
 */
@Repository
public interface BedRepository extends JpaRepository<Bed, Long> {

    @Override
    @EntityGraph(attributePaths = {"room"})
    List<Bed> findAll();

    @Override
    @EntityGraph(attributePaths = {"room"})
    org.springframework.data.domain.Page<Bed> findAll(org.springframework.data.domain.Pageable pageable);

    /** Lấy tất cả giường trong một phòng. */
    List<Bed> findByRoomId(Long roomId);

    /** Lấy giường theo trạng thái trong phòng. */
    List<Bed> findByRoomIdAndStatus(Long roomId, BedStatus status);

    /** Kiểm tra mã giường đã tồn tại. */
    boolean existsByBedCode(String bedCode);

    /** Tìm giường theo mã. */
    Optional<Bed> findByBedCode(String bedCode);

    /** Đếm số giường trống trong phòng. */
    long countByRoomIdAndStatus(Long roomId, BedStatus status);

    long countByRoomId(Long roomId);

    /**
     * Lấy giường kèm thông tin phòng.
     */
    @Query("SELECT b FROM Bed b JOIN FETCH b.room WHERE b.id = :id")
    Optional<Bed> findByIdWithRoom(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Bed b JOIN FETCH b.room WHERE b.id = :id")
    Optional<Bed> findByIdForUpdate(@Param("id") Long id);

    /**
     * Lấy tất cả giường trống (để chọn khi tạo hợp đồng).
     */
    @Query("SELECT b FROM Bed b JOIN FETCH b.room r WHERE r.id = :roomId AND b.status = 'AVAILABLE'")
    List<Bed> findAvailableBedsByRoom(@Param("roomId") Long roomId);
}

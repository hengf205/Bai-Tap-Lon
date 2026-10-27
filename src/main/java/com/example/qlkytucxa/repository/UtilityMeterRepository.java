package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.UtilityMeter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý chỉ số điện nước.
 */
@Repository
public interface UtilityMeterRepository extends JpaRepository<UtilityMeter, Long> {

    /** Lấy tất cả chỉ số của một phòng, sắp xếp mới nhất trước. */
    List<UtilityMeter> findByRoomIdOrderByRecordingDateDesc(Long roomId);

    /**
     * Lấy bản ghi mới nhất của phòng.
     * Dùng để lấy chỉ số cũ khi nhập chỉ số mới.
     */
    Optional<UtilityMeter> findTopByRoomIdOrderByRecordingDateDesc(Long roomId);

    /**
     * Lấy chỉ số điện nước theo tháng/năm của phòng.
     */
    @Query("SELECT um FROM UtilityMeter um WHERE um.room.id = :roomId " +
           "AND YEAR(um.recordingDate) = :year " +
           "AND MONTH(um.recordingDate) = :month")
    Optional<UtilityMeter> findByRoomAndYearMonth(@Param("roomId") Long roomId,
                                                   @Param("year") int year,
                                                   @Param("month") int month);

    /**
     * Tìm kiếm chỉ số theo phòng và khoảng thời gian.
     */
    @EntityGraph(attributePaths = {"room"})
    @Query("SELECT um FROM UtilityMeter um JOIN um.room r " +
           "WHERE (:roomId IS NULL OR r.id = :roomId) " +
           "AND (:fromDate IS NULL OR um.recordingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR um.recordingDate <= :toDate) " +
           "ORDER BY um.recordingDate DESC")
    Page<UtilityMeter> searchUtilityMeters(@Param("roomId") Long roomId,
                                            @Param("fromDate") LocalDate fromDate,
                                            @Param("toDate") LocalDate toDate,
                                            Pageable pageable);
}

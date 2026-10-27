package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.RoomService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý dịch vụ của từng phòng.
 */
@Repository
public interface RoomServiceRepository extends JpaRepository<RoomService, Long> {

    /** Lấy tất cả dịch vụ của một phòng. */
    List<RoomService> findByRoomId(Long roomId);

    /** Xóa toàn bộ dịch vụ của phòng (khi cập nhật lại). */
    void deleteByRoomId(Long roomId);

    /** Kiểm tra phòng đã có dịch vụ chưa. */
    boolean existsByRoomIdAndServiceId(Long roomId, Long serviceId);

    boolean existsByServiceId(Long serviceId);
}

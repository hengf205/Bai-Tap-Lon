package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.entity.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý phòng ở.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    /** Tìm phòng theo mã. */
    Optional<Room> findByRoomCode(String roomCode);

    /** Kiểm tra mã phòng đã tồn tại. */
    boolean existsByRoomCode(String roomCode);

    /** Lấy phòng theo trạng thái. */
    List<Room> findByStatus(RoomStatus status);

    /** Lấy phòng theo tầng. */
    List<Room> findByFloorId(Long floorId);

    long countByFloorId(Long floorId);

    /** Đếm số phòng theo trạng thái. */
    long countByStatus(RoomStatus status);

    /** Đếm tổng số phòng. */
    long count();

    /**
     * Tìm kiếm phòng theo mã, tòa nhà, loại và trạng thái (phân trang).
     */
    @Query("SELECT r FROM Room r JOIN r.floor f JOIN f.building b WHERE " +
           "(:keyword IS NULL OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:buildingId IS NULL OR b.id = :buildingId) " +
           "AND (:status IS NULL OR r.status = :status) " +
           "AND (:roomType IS NULL OR r.roomType = :roomType)")
    Page<Room> searchRooms(@Param("keyword") String keyword,
                           @Param("buildingId") Long buildingId,
                           @Param("status") RoomStatus status,
                           @Param("roomType") RoomType roomType,
                           Pageable pageable);

    /**
     * Lấy danh sách phòng theo tòa nhà.
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.floor f WHERE f.building.id = :buildingId AND r.status <> 'MAINTENANCE' ORDER BY r.roomCode ASC")
    List<Room> findByBuildingId(@Param("buildingId") Long buildingId);

    /**
     * Lấy phòng kèm thông tin tầng và tòa nhà.
     */
    @Query("SELECT r FROM Room r JOIN FETCH r.floor f JOIN FETCH f.building WHERE r.id = :id")
    Optional<Room> findByIdWithFloorAndBuilding(@Param("id") Long id);

    /**
     * Lấy tất cả phòng của tòa nhà theo trạng thái.
     */
    @Query("SELECT r FROM Room r JOIN r.floor f WHERE f.building.id = :buildingId AND r.status = :status")
    List<Room> findByBuildingIdAndStatus(@Param("buildingId") Long buildingId,
                                          @Param("status") RoomStatus status);

    /**
     * Thống kê số phòng theo trạng thái theo từng tòa nhà (cho dashboard).
     */
    @Query("SELECT b.buildingName, r.status, COUNT(r) " +
           "FROM Room r JOIN r.floor f JOIN f.building b " +
           "GROUP BY b.buildingName, r.status")
    List<Object[]> countRoomsByBuildingAndStatus();
}

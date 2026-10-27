package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Floor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý tầng trong tòa nhà.
 */
@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    @Override
    @EntityGraph(attributePaths = {"building"})
    List<Floor> findAll();

    @Override
    @EntityGraph(attributePaths = {"building"})
    List<Floor> findAll(org.springframework.data.domain.Sort sort);

    /** Lấy tất cả tầng theo tòa nhà. */
    List<Floor> findByBuildingIdOrderByFloorNumberAsc(Long buildingId);

    /** Kiểm tra tầng đã tồn tại trong tòa chưa. */
    boolean existsByBuildingIdAndFloorNumber(Long buildingId, Integer floorNumber);

    /** Đếm số tầng trong một tòa nhà. */
    long countByBuildingId(Long buildingId);

    /**
     * Lấy tầng kèm danh sách phòng.
     */
    @Query("SELECT f FROM Floor f LEFT JOIN FETCH f.rooms WHERE f.id = :id")
    Optional<Floor> findByIdWithRooms(@Param("id") Long id);

    /**
     * Lấy tất cả tầng của tòa kèm phòng.
     */
    @Query("SELECT DISTINCT f FROM Floor f LEFT JOIN FETCH f.rooms WHERE f.building.id = :buildingId ORDER BY f.floorNumber ASC")
    List<Floor> findByBuildingIdWithRooms(@Param("buildingId") Long buildingId);
}

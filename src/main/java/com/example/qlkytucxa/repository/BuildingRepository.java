package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.BuildingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý tòa nhà.
 */
@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    /** Tìm theo mã tòa nhà. */
    Optional<Building> findByBuildingCode(String buildingCode);

    /** Kiểm tra mã tòa đã tồn tại. */
    boolean existsByBuildingCode(String buildingCode);

    /** Lấy tất cả tòa theo trạng thái. */
    List<Building> findByStatus(BuildingStatus status);

    /** Đếm số tòa nhà đang hoạt động. */
    long countByStatus(BuildingStatus status);

    /**
     * Tìm kiếm tòa nhà theo tên hoặc mã (có phân trang).
     */
    @Query("SELECT b FROM Building b WHERE " +
           "(:keyword IS NULL OR LOWER(b.buildingName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(b.buildingCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR b.status = :status)")
    Page<Building> searchBuildings(@Param("keyword") String keyword,
                                   @Param("status") BuildingStatus status,
                                   Pageable pageable);

    /**
     * Lấy tất cả tòa kèm thông tin tầng (để hiển thị dashboard).
     */
    @Query("SELECT DISTINCT b FROM Building b LEFT JOIN FETCH b.floors")
    List<Building> findAllWithFloors();
}

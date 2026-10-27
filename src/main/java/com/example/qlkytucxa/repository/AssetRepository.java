package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý tài sản trong phòng.
 */
@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {

    @Override
    @EntityGraph(attributePaths = {"room"})
    Page<Asset> findAll(Pageable pageable);

    /** Lấy tất cả tài sản trong phòng. */
    List<Asset> findByRoomId(Long roomId);

    /** Đếm số tài sản trong phòng. */
    long countByRoomId(Long roomId);
}

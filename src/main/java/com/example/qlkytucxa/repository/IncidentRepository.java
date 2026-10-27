package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Incident;
import com.example.qlkytucxa.entity.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý sự cố / báo hỏng.
 */
@Repository
public interface IncidentRepository extends JpaRepository<Incident, Long> {

    /** Lấy sự cố theo phòng. */
    List<Incident> findByRoomIdOrderByCreatedAtDesc(Long roomId);

    /** Lấy sự cố theo người thuê. */
    @EntityGraph(attributePaths = {"room"})
    List<Incident> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    boolean existsByTenantId(Long tenantId);

    /** Đếm sự cố theo trạng thái. */
    long countByStatus(IncidentStatus status);

    /**
     * Tìm kiếm sự cố theo tiêu đề, phòng, trạng thái (phân trang).
     */
    @EntityGraph(attributePaths = {"room", "tenant"})
    @Query("SELECT i FROM Incident i " +
           "JOIN i.room r " +
           "LEFT JOIN i.tenant t " +
           "WHERE (:keyword IS NULL " +
           "  OR LOWER(i.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR (t IS NOT NULL AND LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))) " +
           "AND (:status IS NULL OR i.status = :status)")
    Page<Incident> searchIncidents(@Param("keyword") String keyword,
                                    @Param("status") IncidentStatus status,
                                    Pageable pageable);

    /**
     * Lấy sự cố của người thuê theo username.
     */
    @EntityGraph(attributePaths = {"room", "tenant"})
    @Query("SELECT i FROM Incident i " +
           "JOIN i.tenant t " +
           "JOIN t.user u " +
           "WHERE u.username = :username " +
           "ORDER BY i.createdAt DESC")
    List<Incident> findByTenantUsername(@Param("username") String username);
}

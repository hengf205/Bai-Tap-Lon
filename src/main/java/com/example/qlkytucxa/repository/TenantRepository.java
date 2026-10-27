package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository quản lý thông tin người thuê / sinh viên.
 */
@Repository
public interface TenantRepository extends JpaRepository<Tenant, Long> {

    /** Tìm tenant theo user_id. */
    Optional<Tenant> findByUserId(Long userId);

    /** Tìm tenant theo mã sinh viên. */
    Optional<Tenant> findByStudentCode(String studentCode);

    /** Tìm tenant theo CMND/CCCD. */
    Optional<Tenant> findByIdentityCard(String identityCard);

    /** Kiểm tra mã sinh viên đã tồn tại. */
    boolean existsByStudentCode(String studentCode);

    boolean existsByEmail(String email);

    /** Kiểm tra CMND đã tồn tại. */
    boolean existsByIdentityCard(String identityCard);

    /** Đếm số người thuê đang hoạt động. */
    long countByStatus(TenantStatus status);

    /**
     * Tìm kiếm người thuê theo tên, mã sinh viên, số điện thoại (phân trang).
     */
    @Query("SELECT t FROM Tenant t WHERE " +
           "(:keyword IS NULL OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.studentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR t.phone LIKE CONCAT('%', :keyword, '%') " +
           "OR LOWER(t.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR t.status = :status)")
    Page<Tenant> searchTenants(@Param("keyword") String keyword,
                                @Param("status") TenantStatus status,
                                Pageable pageable);

    /**
     * Lấy tenant kèm hợp đồng đang active.
     */
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.contracts c WHERE t.id = :id AND (c IS NULL OR c.status = 'ACTIVE')")
    Optional<Tenant> findByIdWithActiveContract(@Param("id") Long id);

    /**
     * Lấy tenant kèm user account.
     */
    @Query("SELECT t FROM Tenant t LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Tenant> findByIdWithUser(@Param("id") Long id);

    /**
     * Tìm tenant theo username.
     */
    @Query("SELECT t FROM Tenant t JOIN t.user u WHERE u.username = :username")
    Optional<Tenant> findByUsername(@Param("username") String username);
}

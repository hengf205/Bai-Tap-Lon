package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.User;
import com.example.qlkytucxa.entity.Role;
import com.example.qlkytucxa.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository quản lý người dùng hệ thống.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Tìm user theo username (dùng cho Spring Security login). */
    Optional<User> findByUsername(String username);

    /** Tìm user theo email. */
    Optional<User> findByEmail(String email);

    /** Kiểm tra username đã tồn tại chưa. */
    boolean existsByUsername(String username);

    /** Kiểm tra email đã tồn tại chưa. */
    boolean existsByEmail(String email);

    /** Đếm số user theo role. */
    long countByRole(Role role);

    /** Đếm số user đang active. */
    long countByStatus(UserStatus status);

    /**
     * Tìm kiếm user theo tên, username hoặc email (có phân trang).
     */
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL " +
           "OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(@Param("keyword") String keyword,
                           @Param("role") Role role,
                           Pageable pageable);
}

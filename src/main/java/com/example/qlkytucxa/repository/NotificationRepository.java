package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository quản lý thông báo hệ thống.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /** Lấy tất cả thông báo của một user, mới nhất trước. */
    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    /** Lấy thông báo chưa đọc của user. */
    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    /** Đếm số thông báo chưa đọc. */
    long countByUserIdAndIsRead(Long userId, Boolean isRead);

    /**
     * Đánh dấu tất cả thông báo của user là đã đọc.
     */
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId")
    void markAllAsRead(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :notificationId AND n.user.id = :userId")
    int markAsReadForUser(@Param("notificationId") Long notificationId, @Param("userId") Long userId);
}

package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Payment;
import com.example.qlkytucxa.entity.PaymentMethod;
import com.example.qlkytucxa.entity.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý lịch sử thanh toán.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /** Tìm thanh toán theo mã. */
    Optional<Payment> findByPaymentCode(String paymentCode);

    /** Kiểm tra mã thanh toán đã tồn tại. */
    boolean existsByPaymentCode(String paymentCode);

    /** Lấy thanh toán theo hóa đơn. */
    List<Payment> findByInvoiceIdOrderByPaymentDateDesc(Long invoiceId);

    long countByInvoiceId(Long invoiceId);

    /**
     * Tìm kiếm thanh toán theo mã, hóa đơn, phương thức (phân trang).
     */
    @EntityGraph(attributePaths = {"invoice", "invoice.contract", "invoice.contract.tenant"})
    @Query("SELECT p FROM Payment p JOIN p.invoice i " +
           "WHERE (:keyword IS NULL " +
           "  OR LOWER(p.paymentCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:method IS NULL OR p.paymentMethod = :method) " +
           "AND (:status IS NULL OR p.status = :status)")
    Page<Payment> searchPayments(@Param("keyword") String keyword,
                                  @Param("method") PaymentMethod method,
                                  @Param("status") PaymentStatus status,
                                  Pageable pageable);

    /**
     * Lấy lịch sử thanh toán theo tenant.
     */
    @EntityGraph(attributePaths = {"invoice", "invoice.contract", "invoice.contract.tenant"})
    @Query("SELECT p FROM Payment p " +
           "JOIN p.invoice i " +
           "JOIN i.contract c " +
           "JOIN c.tenant t " +
           "JOIN t.user u " +
           "WHERE u.username = :username " +
           "ORDER BY p.paymentDate DESC")
    List<Payment> findByTenantUsername(@Param("username") String username);
}

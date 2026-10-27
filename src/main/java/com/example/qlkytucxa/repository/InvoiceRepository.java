package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Invoice;
import com.example.qlkytucxa.entity.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý hóa đơn.
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Tìm hóa đơn theo mã. */
    Optional<Invoice> findByInvoiceCode(String invoiceCode);

    @Query("SELECT DISTINCT i.contract.id FROM Invoice i WHERE i.status = 'UNPAID' AND i.contract.status = 'PENDING'")
    List<Long> findPendingRentalContractIdsWithUnpaidInvoices();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @EntityGraph(attributePaths = {"contract", "contract.tenant", "contract.bed", "contract.room"})
    @Query("SELECT i FROM Invoice i WHERE i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"contract", "contract.tenant", "contract.room", "contract.bed"})
    @Query("SELECT i FROM Invoice i JOIN i.contract c JOIN c.tenant t JOIN t.user u " +
           "WHERE i.id = :invoiceId AND u.username = :username")
    Optional<Invoice> findByIdAndTenantUsername(@Param("invoiceId") Long invoiceId, @Param("username") String username);

    @EntityGraph(attributePaths = {"contract", "contract.tenant", "contract.bed", "contract.room"})
    @Query("SELECT i FROM Invoice i JOIN i.contract c WHERE i.status = :invoiceStatus " +
           "AND i.dueDate < :today AND c.status = :contractStatus")
    List<Invoice> findExpiredRentalInvoices(@Param("invoiceStatus") InvoiceStatus invoiceStatus,
                                            @Param("contractStatus") com.example.qlkytucxa.entity.ContractStatus contractStatus,
                                            @Param("today") LocalDate today);

    /** Kiểm tra mã hóa đơn đã tồn tại. */
    boolean existsByInvoiceCode(String invoiceCode);

    /** Lấy hóa đơn theo trạng thái. */
    List<Invoice> findByStatus(InvoiceStatus status);

    /** Lấy hóa đơn của một hợp đồng. */
    List<Invoice> findByContractIdOrderByBillingMonthDesc(Long contractId);

    /** Đếm hóa đơn chưa thanh toán. */
    long countByStatus(InvoiceStatus status);

    /**
     * Tìm kiếm hóa đơn theo mã, tên người thuê, phòng, tháng, trạng thái (phân trang).
     */
    @EntityGraph(attributePaths = {"contract", "contract.tenant", "contract.room"})
    @Query("SELECT i FROM Invoice i " +
           "JOIN i.contract c " +
           "JOIN c.tenant t " +
           "JOIN c.room r " +
           "WHERE (:keyword IS NULL " +
           "  OR LOWER(i.invoiceCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR i.status = :status) " +
           "AND (:month IS NULL OR MONTH(i.billingMonth) = :month) " +
           "AND (:year IS NULL OR YEAR(i.billingMonth) = :year)")
    Page<Invoice> searchInvoices(@Param("keyword") String keyword,
                                  @Param("status") InvoiceStatus status,
                                  @Param("month") Integer month,
                                  @Param("year") Integer year,
                                  Pageable pageable);

    /**
     * Lấy hóa đơn của người thuê theo username.
     */
    @EntityGraph(attributePaths = {"contract", "contract.tenant", "contract.room"})
    @Query("SELECT i FROM Invoice i " +
           "JOIN i.contract c " +
           "JOIN c.tenant t " +
           "JOIN t.user u " +
           "WHERE u.username = :username " +
           "ORDER BY i.billingMonth DESC")
    List<Invoice> findByTenantUsername(@Param("username") String username);

    /**
     * Tính tổng doanh thu theo tháng và năm.
     */
    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i " +
           "WHERE i.status = 'PAID' " +
           "AND MONTH(i.billingMonth) = :month " +
           "AND YEAR(i.billingMonth) = :year")
    BigDecimal sumRevenueByMonthAndYear(@Param("month") int month, @Param("year") int year);

    /**
     * Tính doanh thu theo từng tháng trong năm (cho biểu đồ).
     */
    @Query("SELECT MONTH(i.billingMonth), COALESCE(SUM(i.totalAmount), 0) " +
           "FROM Invoice i WHERE i.status = 'PAID' AND YEAR(i.billingMonth) = :year " +
           "GROUP BY MONTH(i.billingMonth) ORDER BY MONTH(i.billingMonth)")
    List<Object[]> getMonthlyRevenue(@Param("year") int year);

    @Query("SELECT MONTH(i.billingMonth), COALESCE(SUM(i.roomPrice), 0) " +
           "FROM Invoice i WHERE i.status = 'PAID' AND YEAR(i.billingMonth) = :year " +
           "GROUP BY MONTH(i.billingMonth) ORDER BY MONTH(i.billingMonth)")
    List<Object[]> getMonthlyRoomRevenue(@Param("year") int year);

    @Query("SELECT MONTH(i.billingMonth), COALESCE(SUM(i.serviceAmount), 0) " +
           "FROM Invoice i WHERE i.status = 'PAID' AND YEAR(i.billingMonth) = :year " +
           "GROUP BY MONTH(i.billingMonth) ORDER BY MONTH(i.billingMonth)")
    List<Object[]> getMonthlyServiceRevenue(@Param("year") int year);

    /**
     * Lấy hóa đơn quá hạn (chưa thanh toán và qua ngày due_date).
     */
    @Query("SELECT i FROM Invoice i WHERE i.status = 'UNPAID' AND i.dueDate < :today")
    List<Invoice> findOverdueInvoices(@Param("today") LocalDate today);

    /**
     * Tổng tiền điện nước tháng hiện tại.
     */
    @Query("SELECT COALESCE(SUM(i.electricityAmount + i.waterAmount), 0) FROM Invoice i " +
           "WHERE MONTH(i.billingMonth) = :month AND YEAR(i.billingMonth) = :year")
    BigDecimal sumUtilityByMonthAndYear(@Param("month") int month, @Param("year") int year);
}

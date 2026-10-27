package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository quản lý hợp đồng thuê phòng.
 */
@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    @Override
    @EntityGraph(attributePaths = {"tenant", "room"})
    List<Contract> findAll();

    /** Tìm hợp đồng theo mã. */
    Optional<Contract> findByContractCode(String contractCode);

    /** Kiểm tra mã hợp đồng đã tồn tại. */
    boolean existsByContractCode(String contractCode);

    /**
     * Kiểm tra giường đã có hợp đồng ACTIVE chưa (để ngăn tạo trùng).
     * Quan trọng: logic nghiệp vụ tránh xung đột hợp đồng.
     */
    boolean existsByBedIdAndStatus(Long bedId, ContractStatus status);

    /** Bỏ qua hợp đồng hiện tại khi kiểm tra giường trong lúc cập nhật. */
    boolean existsByBedIdAndStatusAndIdNot(Long bedId, ContractStatus status, Long id);

    /**
     * Kiểm tra người thuê đã có hợp đồng ACTIVE chưa.
     */
    boolean existsByTenantIdAndStatus(Long tenantId, ContractStatus status);

    /** Bỏ qua hợp đồng hiện tại khi kiểm tra người thuê trong lúc cập nhật. */
    boolean existsByTenantIdAndStatusAndIdNot(Long tenantId, ContractStatus status, Long id);

    /** Lấy hợp đồng active của người thuê. */
    @EntityGraph(attributePaths = {"tenant", "room", "room.floor", "room.floor.building"})
    Optional<Contract> findByTenantIdAndStatus(Long tenantId, ContractStatus status);

    /** Lấy tất cả hợp đồng của người thuê. */
    @EntityGraph(attributePaths = {"tenant", "room", "room.floor", "room.floor.building", "bed"})
    List<Contract> findByTenantIdOrderByStartDateDesc(Long tenantId);

    /** Lấy tất cả hợp đồng của phòng. */
    @EntityGraph(attributePaths = {"tenant", "bed"})
    List<Contract> findByRoomIdOrderByStartDateDesc(Long roomId);

    List<Contract> findByBedIdOrderByStartDateDesc(Long bedId);

    /** Đếm số hợp đồng theo trạng thái. */
    long countByStatus(ContractStatus status);

    @Query("SELECT b.buildingName, COUNT(DISTINCT c.tenant.id) FROM Contract c " +
           "JOIN c.room r JOIN r.floor f JOIN f.building b " +
           "WHERE c.status = 'ACTIVE' GROUP BY b.buildingName ORDER BY b.buildingName")
    List<Object[]> countActiveTenantsByBuilding();

    /**
     * Tìm kiếm hợp đồng theo mã, tên người thuê hoặc phòng (phân trang).
     */
    @Query("SELECT c FROM Contract c " +
           "JOIN FETCH c.tenant t " +
           "JOIN FETCH c.room r " +
           "WHERE (:keyword IS NULL " +
           "  OR LOWER(c.contractCode) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(t.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "  OR LOWER(r.roomCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:status IS NULL OR c.status = :status)")
    Page<Contract> searchContracts(@Param("keyword") String keyword,
                                    @Param("status") ContractStatus status,
                                    Pageable pageable);

    /**
     * Tìm các hợp đồng sắp hết hạn (trong vòng :days ngày tới).
     * Dùng để gửi thông báo nhắc nhở.
     */
    @Query("SELECT c FROM Contract c WHERE c.status = 'ACTIVE' " +
           "AND c.endDate BETWEEN :today AND :endDate")
    List<Contract> findContractsExpiringSoon(@Param("today") LocalDate today,
                                              @Param("endDate") LocalDate endDate);

    /**
     * Lấy hợp đồng kèm đầy đủ thông tin liên quan.
     */
    @Query("SELECT c FROM Contract c " +
           "JOIN FETCH c.tenant t " +
           "JOIN FETCH c.room r " +
           "JOIN FETCH r.floor f " +
           "JOIN FETCH f.building b " +
           "WHERE c.id = :id")
    Optional<Contract> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT c FROM Contract c JOIN FETCH c.tenant t JOIN FETCH c.room r LEFT JOIN FETCH c.bed b WHERE c.id = :id")
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Contract> findByIdForUpdate(@Param("id") Long id);
}

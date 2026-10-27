package com.example.qlkytucxa.repository;

import com.example.qlkytucxa.entity.Service;
import com.example.qlkytucxa.entity.ServiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository quản lý dịch vụ.
 */
@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    /** Lấy tất cả dịch vụ đang hoạt động. */
    List<Service> findByStatus(ServiceStatus status);

    /** Tìm theo tên dịch vụ. */
    boolean existsByServiceName(String serviceName);
}

package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServiceCatalogService {
    Page<Service> getAll(Pageable pageable);
    Service getById(Long id);
    Service save(Service service);
    void delete(Long id);
}

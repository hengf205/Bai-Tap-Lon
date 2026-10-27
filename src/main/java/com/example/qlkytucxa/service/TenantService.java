package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.TenantStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TenantService extends CrudService<Tenant> {
    Page<Tenant> search(String keyword, TenantStatus status, Pageable pageable);
    Tenant getByUsername(String username);
}

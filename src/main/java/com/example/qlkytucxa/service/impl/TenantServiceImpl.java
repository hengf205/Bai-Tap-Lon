package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.TenantStatus;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.IncidentRepository;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.service.TenantService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantServiceImpl extends AbstractCrudService<Tenant> implements TenantService {
    private final TenantRepository repository;
    private final ContractRepository contracts;
    private final IncidentRepository incidents;
    public TenantServiceImpl(TenantRepository repository, ContractRepository contracts, IncidentRepository incidents) { super(repository, "Người thuê"); this.repository = repository; this.contracts = contracts; this.incidents = incidents; }
    @Override public Page<Tenant> search(String keyword, TenantStatus status, Pageable pageable) {
        return repository.searchTenants(keyword == null || keyword.isBlank() ? null : keyword.trim(), status, pageable);
    }
    @Override public Tenant getByUsername(String username) {
        return repository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Người thuê", -1L));
    }
    @Override @Transactional public Tenant save(Tenant value) {
        if (value.getStudentCode() != null && !value.getStudentCode().isBlank()) {
            repository.findByStudentCode(value.getStudentCode()).filter(old -> !old.getId().equals(value.getId()))
                    .ifPresent(old -> { throw new DuplicateResourceException("Mã sinh viên đã tồn tại"); });
        }
        if (value.getIdentityCard() != null && !value.getIdentityCard().isBlank()) {
            repository.findByIdentityCard(value.getIdentityCard()).filter(old -> !old.getId().equals(value.getId()))
                    .ifPresent(old -> { throw new DuplicateResourceException("Số CMND/CCCD đã tồn tại"); });
        }
        return repository.save(value);
    }
    @Override @Transactional public void delete(Long id) {
        if (!contracts.findByTenantIdOrderByStartDateDesc(id).isEmpty() || incidents.existsByTenantId(id)) throw new InvalidOperationException("Không thể xóa hồ sơ đã phát sinh hợp đồng hoặc sự cố; hãy chuyển sang trạng thái không hoạt động");
        super.delete(id);
    }
}

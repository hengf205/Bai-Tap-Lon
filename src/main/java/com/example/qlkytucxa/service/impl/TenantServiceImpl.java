package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Tenant;
import com.example.qlkytucxa.entity.TenantStatus;
import com.example.qlkytucxa.entity.ContractStatus;
import com.example.qlkytucxa.entity.UserStatus;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.TenantRepository;
import com.example.qlkytucxa.repository.ContractRepository;
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
    public TenantServiceImpl(TenantRepository repository, ContractRepository contracts) { super(repository, "Người thuê"); this.repository = repository; this.contracts = contracts; }
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
        Tenant tenant = repository.findByIdWithUser(id)
                .orElseThrow(() -> new ResourceNotFoundException("Người thuê", id));
        boolean hasOpenContract = contracts.findByTenantIdOrderByStartDateDesc(id).stream()
                .anyMatch(contract -> contract.getStatus() == ContractStatus.ACTIVE || contract.getStatus() == ContractStatus.PENDING);
        if (hasOpenContract) {
            throw new InvalidOperationException("Người thuê đang có hợp đồng chờ duyệt hoặc còn hiệu lực. Hãy xử lý hợp đồng trước khi ngừng hồ sơ.");
        }
        tenant.setStatus(TenantStatus.INACTIVE);
        if (tenant.getUser() != null) tenant.getUser().setStatus(UserStatus.INACTIVE);
        repository.save(tenant);
    }
}

package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.*;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.*;
import com.example.qlkytucxa.service.ContractService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ContractServiceImpl extends AbstractCrudService<Contract> implements ContractService {
    private final ContractRepository repository;
    private final TenantRepository tenants;
    private final RoomRepository rooms;
    private final BedRepository beds;

    public ContractServiceImpl(ContractRepository repository, TenantRepository tenants, RoomRepository rooms, BedRepository beds) {
        super(repository, "Hợp đồng"); this.repository = repository; this.tenants = tenants; this.rooms = rooms; this.beds = beds;
    }

    @Override @Transactional public Contract create(Contract value) {
        validateDatesAndCode(value, null);
        Tenant tenant = tenants.findById(value.getTenant().getId()).orElseThrow(() -> new InvalidOperationException("Người thuê không tồn tại"));
        Room room = rooms.findById(value.getRoom().getId()).orElseThrow(() -> new InvalidOperationException("Phòng không tồn tại"));
        value.setTenant(tenant); value.setRoom(room);
        if (value.getStatus() == ContractStatus.ACTIVE) {
            if (repository.existsByTenantIdAndStatus(tenant.getId(), ContractStatus.ACTIVE)) throw new InvalidOperationException("Người thuê đã có hợp đồng đang hiệu lực");
            if (value.getBed() == null) throw new InvalidOperationException("Hợp đồng đang hiệu lực phải chọn giường");
            Bed bed = beds.findById(value.getBed().getId()).orElseThrow(() -> new InvalidOperationException("Giường không tồn tại"));
            if (!bed.getRoom().getId().equals(room.getId()) || bed.getStatus() != BedStatus.AVAILABLE) throw new InvalidOperationException("Giường không thuộc phòng hoặc đã được sử dụng");
            if (repository.existsByBedIdAndStatus(bed.getId(), ContractStatus.ACTIVE)) throw new InvalidOperationException("Giường đã có hợp đồng hiệu lực");
            bed.setStatus(BedStatus.OCCUPIED); value.setBed(bed);
        }
        Contract saved = repository.save(value);
        refreshRoomStatus(room);
        return saved;
    }

    @Override @Transactional public Contract update(Long id, Contract value) {
        Contract current = getById(id);
        if (current.getStatus() == ContractStatus.PENDING) {
            throw new InvalidOperationException("Yêu cầu thuê đang chờ chỉ được xử lý bằng nút duyệt hoặc từ chối.");
        }
        value.setId(id);
        validateDatesAndCode(value, id);
        if (current.getStatus() == ContractStatus.ACTIVE && value.getStatus() != ContractStatus.ACTIVE) releaseBed(current);
        Tenant tenant = tenants.findById(value.getTenant().getId()).orElseThrow(() -> new InvalidOperationException("Người thuê không tồn tại"));
        Room room = rooms.findById(value.getRoom().getId()).orElseThrow(() -> new InvalidOperationException("Phòng không tồn tại"));
        value.setTenant(tenant);
        value.setRoom(room);
        if (value.getStatus() == ContractStatus.ACTIVE && (current.getStatus() != ContractStatus.ACTIVE || !sameBed(current, value)
                || !current.getTenant().getId().equals(tenant.getId()) || !current.getRoom().getId().equals(room.getId()))) {
            if (repository.existsByTenantIdAndStatusAndIdNot(tenant.getId(), ContractStatus.ACTIVE, id)) throw new InvalidOperationException("Người thuê đã có hợp đồng đang hiệu lực");
            if (value.getBed() == null) throw new InvalidOperationException("Hợp đồng đang hiệu lực phải chọn giường");
            Bed newBed = beds.findById(value.getBed().getId()).orElseThrow(() -> new InvalidOperationException("Giường không tồn tại"));
            if (!newBed.getRoom().getId().equals(room.getId())) throw new InvalidOperationException("Giường không thuộc phòng đã chọn");
            if (newBed.getStatus() != BedStatus.AVAILABLE && !sameBed(current, value)) throw new InvalidOperationException("Giường đã được sử dụng");
            newBed.setStatus(BedStatus.OCCUPIED); value.setBed(newBed);
        }
        Contract saved = repository.save(value);
        refreshRoomStatus(current.getRoom()); refreshRoomStatus(saved.getRoom());
        return saved;
    }

    @Override @Transactional public void finish(Long id) {
        Contract contract = getById(id);
        contract.setStatus(ContractStatus.EXPIRED);
        releaseBed(contract);
        repository.save(contract);
        refreshRoomStatus(contract.getRoom());
    }

    @Override public Page<Contract> search(String keyword, ContractStatus status, Pageable pageable) {
        return repository.searchContracts(keyword == null || keyword.isBlank() ? null : keyword.trim(), status, pageable);
    }

    private void validateDatesAndCode(Contract value, Long id) {
        if (value.getStartDate() == null || value.getEndDate() == null || !value.getEndDate().isAfter(value.getStartDate())) throw new InvalidOperationException("Ngày kết thúc phải sau ngày bắt đầu");
        if (value.getContractCode() == null || value.getContractCode().isBlank()) {
            value.setContractCode("HD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }
        repository.findByContractCode(value.getContractCode()).filter(old -> !old.getId().equals(id)).ifPresent(old -> { throw new DuplicateResourceException("Mã hợp đồng đã tồn tại"); });
    }
    private void releaseBed(Contract contract) { if (contract.getBed() != null) { contract.getBed().setStatus(BedStatus.AVAILABLE); beds.save(contract.getBed()); } }
    private boolean sameBed(Contract first, Contract second) { return first.getBed() != null && second.getBed() != null && first.getBed().getId().equals(second.getBed().getId()); }
    private void refreshRoomStatus(Room room) {
        long occupied = beds.countByRoomIdAndStatus(room.getId(), BedStatus.OCCUPIED);
        long reserved = beds.countByRoomIdAndStatus(room.getId(), BedStatus.RESERVED);
        if (room.getStatus() != RoomStatus.MAINTENANCE) room.setStatus(occupied + reserved == 0 ? RoomStatus.AVAILABLE
                : (occupied + reserved >= room.getCapacity() ? RoomStatus.FULL : RoomStatus.OCCUPIED));
        rooms.save(room);
    }
}

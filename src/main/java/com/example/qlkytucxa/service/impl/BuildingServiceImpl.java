package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.BuildingStatus;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.repository.BuildingRepository;
import com.example.qlkytucxa.repository.FloorRepository;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.service.BuildingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BuildingServiceImpl extends AbstractCrudService<Building> implements BuildingService {
    private final BuildingRepository repository;
    private final FloorRepository floors;
    public BuildingServiceImpl(BuildingRepository repository, FloorRepository floors) { super(repository, "Tòa nhà"); this.repository = repository; this.floors = floors; }
    @Override public Page<Building> search(String keyword, BuildingStatus status, Pageable pageable) {
        String normalized = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return repository.searchBuildings(normalized, status, pageable);
    }
    @Override @Transactional public Building save(Building value) {
        repository.findByBuildingCode(value.getBuildingCode()).filter(old -> !old.getId().equals(value.getId()))
                .ifPresent(old -> { throw new DuplicateResourceException("Mã tòa nhà đã tồn tại"); });
        return repository.save(value);
    }
    @Override @Transactional public void delete(Long id) {
        if (floors.countByBuildingId(id) > 0) throw new InvalidOperationException("Không thể xóa tòa nhà đang có tầng; hãy chuyển trạng thái tòa nhà thay vì xóa");
        super.delete(id);
    }
}

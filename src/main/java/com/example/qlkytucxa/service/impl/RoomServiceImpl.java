package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.entity.RoomType;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.UtilityMeterRepository;
import com.example.qlkytucxa.repository.IncidentRepository;
import com.example.qlkytucxa.repository.AssetRepository;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.service.RoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoomServiceImpl extends AbstractCrudService<Room> implements RoomService {
    private final RoomRepository repository;
    private final BedRepository beds;
    private final ContractRepository contracts;
    private final UtilityMeterRepository meters;
    private final IncidentRepository incidents;
    private final AssetRepository assets;
    public RoomServiceImpl(RoomRepository repository, BedRepository beds, ContractRepository contracts,
                           UtilityMeterRepository meters, IncidentRepository incidents, AssetRepository assets) {
        super(repository, "Phòng"); this.repository = repository; this.beds = beds; this.contracts = contracts;
        this.meters = meters; this.incidents = incidents; this.assets = assets;
    }
    @Override public Page<Room> search(String keyword, Long buildingId, RoomStatus status, RoomType type, Pageable pageable) {
        return repository.searchRooms(keyword == null || keyword.isBlank() ? null : keyword.trim(), buildingId, status, type, pageable);
    }
    @Override @Transactional public Room save(Room value) {
        repository.findByRoomCode(value.getRoomCode()).filter(old -> !old.getId().equals(value.getId()))
                .ifPresent(old -> { throw new DuplicateResourceException("Mã phòng đã tồn tại"); });
        return repository.save(value);
    }
    @Override @Transactional public void delete(Long id) {
        boolean hasHistory = beds.countByRoomId(id) > 0
                || !contracts.findByRoomIdOrderByStartDateDesc(id).isEmpty()
                || !meters.findByRoomIdOrderByRecordingDateDesc(id).isEmpty()
                || !incidents.findByRoomIdOrderByCreatedAtDesc(id).isEmpty()
                || assets.countByRoomId(id) > 0;
        if (hasHistory) throw new InvalidOperationException("Không thể xóa phòng đã có giường hoặc dữ liệu phát sinh");
        super.delete(id);
    }
}

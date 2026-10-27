package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Service;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.ServiceRepository;
import com.example.qlkytucxa.repository.RoomServiceRepository;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.service.ServiceCatalogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@org.springframework.stereotype.Service
public class ServiceCatalogServiceImpl implements ServiceCatalogService {
    private final ServiceRepository repository;
    private final RoomServiceRepository roomServices;
    public ServiceCatalogServiceImpl(ServiceRepository repository, RoomServiceRepository roomServices) { this.repository = repository; this.roomServices = roomServices; }
    @Override @Transactional(readOnly = true) public Page<Service> getAll(Pageable pageable) { return repository.findAll(pageable); }
    @Override @Transactional(readOnly = true) public Service getById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Dịch vụ", id)); }
    @Override @Transactional public Service save(Service service) {
        if (repository.existsByServiceName(service.getServiceName()) && (service.getId() == null || repository.findById(service.getId()).map(old -> !old.getServiceName().equalsIgnoreCase(service.getServiceName())).orElse(true))) {
            throw new DuplicateResourceException("Tên dịch vụ đã tồn tại");
        }
        return repository.save(service);
    }
    @Override @Transactional public void delete(Long id) {
        if (!repository.existsById(id)) throw new ResourceNotFoundException("Dịch vụ", id);
        if (roomServices.existsByServiceId(id)) throw new InvalidOperationException("Dịch vụ đang được gán cho phòng; hãy chuyển trạng thái sang ngừng cung cấp");
        repository.deleteById(id);
    }
}

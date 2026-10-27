package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Incident;
import com.example.qlkytucxa.entity.IncidentStatus;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.IncidentRepository;
import com.example.qlkytucxa.service.IncidentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class IncidentServiceImpl extends AbstractCrudService<Incident> implements IncidentService {
    private final IncidentRepository repository;
    public IncidentServiceImpl(IncidentRepository repository) { super(repository, "Sự cố"); this.repository = repository; }
    @Override public Page<Incident> search(String keyword, IncidentStatus status, Pageable pageable) {
        return repository.searchIncidents(keyword == null || keyword.isBlank() ? null : keyword.trim(), status, pageable);
    }
    @Override @Transactional public Incident changeStatus(Long id, IncidentStatus status) {
        Incident incident = getById(id);
        if (status == null) throw new InvalidOperationException("Trạng thái sự cố không hợp lệ");
        incident.setStatus(status);
        incident.setResolvedAt(status == IncidentStatus.RESOLVED ? LocalDateTime.now() : null);
        return repository.save(incident);
    }
}

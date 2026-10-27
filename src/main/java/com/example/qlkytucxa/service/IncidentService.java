package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Incident;
import com.example.qlkytucxa.entity.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IncidentService extends CrudService<Incident> {
    Page<Incident> search(String keyword, IncidentStatus status, Pageable pageable);
    Incident changeStatus(Long id, IncidentStatus status);
}

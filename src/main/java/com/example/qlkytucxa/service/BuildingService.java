package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.BuildingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BuildingService extends CrudService<Building> {
    Page<Building> search(String keyword, BuildingStatus status, Pageable pageable);
}

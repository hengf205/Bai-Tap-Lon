package com.example.qlkytucxa.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CrudService<T> {
    T getById(Long id);
    List<T> getAll();
    Page<T> getAll(Pageable pageable);
    T save(T entity);
    void delete(Long id);
}

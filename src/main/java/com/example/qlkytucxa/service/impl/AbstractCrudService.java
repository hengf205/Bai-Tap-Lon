package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.service.CrudService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional(readOnly = true)
public abstract class AbstractCrudService<T> implements CrudService<T> {
    private final JpaRepository<T, Long> repository;
    private final String resourceName;

    protected AbstractCrudService(JpaRepository<T, Long> repository, String resourceName) {
        this.repository = repository;
        this.resourceName = resourceName;
    }

    @Override
    public T getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(resourceName, id));
    }

    @Override
    public List<T> getAll() {
        return repository.findAll();
    }

    @Override
    public Page<T> getAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Override
    @Transactional
    public T save(T entity) {
        return repository.save(entity);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException(resourceName, id);
        }
        repository.deleteById(id);
    }
}

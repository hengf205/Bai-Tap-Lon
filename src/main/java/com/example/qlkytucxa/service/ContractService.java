package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.ContractStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContractService extends CrudService<Contract> {
    Contract create(Contract contract);
    Contract update(Long id, Contract contract);
    void finish(Long id);
    Page<Contract> search(String keyword, ContractStatus status, Pageable pageable);
}

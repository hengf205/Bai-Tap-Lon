package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.Role;
import com.example.qlkytucxa.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<User> search(String keyword, Role role, Pageable pageable);
    User getById(Long id);
    User save(User user, String rawPassword);
    void delete(Long id);
}

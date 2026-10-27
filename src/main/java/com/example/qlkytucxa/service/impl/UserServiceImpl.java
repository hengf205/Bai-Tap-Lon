package com.example.qlkytucxa.service.impl;

import com.example.qlkytucxa.entity.Role;
import com.example.qlkytucxa.entity.User;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.exception.ResourceNotFoundException;
import com.example.qlkytucxa.repository.UserRepository;
import com.example.qlkytucxa.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) { this.repository = repository; this.passwordEncoder = passwordEncoder; }
    @Override @Transactional(readOnly = true) public Page<User> search(String keyword, Role role, Pageable pageable) {
        return repository.searchUsers(keyword == null || keyword.isBlank() ? null : keyword.trim(), role, pageable);
    }
    @Override @Transactional(readOnly = true) public User getById(Long id) { return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Người dùng", id)); }
    @Override @Transactional public User save(User user, String rawPassword) {
        repository.findByUsername(user.getUsername()).filter(existing -> !existing.getId().equals(user.getId()))
                .ifPresent(existing -> { throw new DuplicateResourceException("Tên đăng nhập đã được sử dụng"); });
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            repository.findByEmail(user.getEmail()).filter(existing -> !existing.getId().equals(user.getId()))
                    .ifPresent(existing -> { throw new DuplicateResourceException("Email đã được sử dụng"); });
        }
        if (rawPassword != null && !rawPassword.isBlank()) user.setPassword(passwordEncoder.encode(rawPassword));
        else if (user.getId() == null) throw new InvalidOperationException("Mật khẩu là bắt buộc khi tạo tài khoản");
        else user.setPassword(getById(user.getId()).getPassword());
        return repository.save(user);
    }
    @Override @Transactional public void delete(Long id) {
        User user = getById(id);
        if (user.getTenant() != null) throw new InvalidOperationException("Tài khoản đang liên kết hồ sơ người thuê; hãy khóa tài khoản thay vì xóa");
        if (user.getRole() == Role.ROLE_ADMIN && repository.countByRole(Role.ROLE_ADMIN) <= 1) {
            throw new InvalidOperationException("Không thể xóa tài khoản quản trị viên cuối cùng");
        }
        repository.delete(user);
    }
}

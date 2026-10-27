package com.example.qlkytucxa.exception;

public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " không tồn tại với mã: " + id);
    }
}

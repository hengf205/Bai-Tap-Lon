package com.example.qlkytucxa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Điểm khởi động của ứng dụng Quản Lý Ký Túc Xá.
 * Chạy bằng: mvn spring-boot:run hoặc nút Run trong IntelliJ IDEA.
 */
@SpringBootApplication
@EnableScheduling
public class QlKyTucXaApplication {

    public static void main(String[] args) {
        SpringApplication.run(QlKyTucXaApplication.class, args);
    }
}

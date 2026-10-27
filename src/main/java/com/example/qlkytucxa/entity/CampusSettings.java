package com.example.qlkytucxa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "campus_settings")
@Getter
@Setter
@NoArgsConstructor
public class CampusSettings {
    public static final long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "institution_name", nullable = false, length = 150)
    private String institutionName = "Đại học Giao thông Vận tải";

    @Column(name = "banner_title", nullable = false, length = 120)
    private String bannerTitle = "Ký túc xá UTC";

    @Column(name = "banner_subtitle", nullable = false, length = 255)
    private String bannerSubtitle = "Hệ thống quản lý ký túc xá Đại học Giao thông Vận tải";

    @Column(name = "banner_image", length = 255)
    private String bannerImage;
}

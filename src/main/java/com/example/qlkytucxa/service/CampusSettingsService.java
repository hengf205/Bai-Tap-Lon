package com.example.qlkytucxa.service;

import com.example.qlkytucxa.entity.CampusSettings;
import com.example.qlkytucxa.repository.CampusSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CampusSettingsService {
    private final CampusSettingsRepository repository;

    public CampusSettingsService(CampusSettingsRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CampusSettings getSettings() {
        return repository.findById(CampusSettings.SINGLETON_ID).orElseGet(() -> {
            CampusSettings settings = new CampusSettings();
            return repository.save(settings);
        });
    }

    @Transactional
    public CampusSettings save(String institutionName, String bannerTitle, String bannerSubtitle, String bannerImage) {
        CampusSettings settings = getSettings();
        settings.setInstitutionName(institutionName.trim());
        settings.setBannerTitle(bannerTitle.trim());
        settings.setBannerSubtitle(bannerSubtitle.trim());
        if (bannerImage != null) settings.setBannerImage(bannerImage);
        return repository.save(settings);
    }
}

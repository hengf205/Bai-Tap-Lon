package com.example.qlkytucxa.config;

import com.example.qlkytucxa.service.CampusSettingsService;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CampusSettingsAdvice {
    private final CampusSettingsService settingsService;

    public CampusSettingsAdvice(CampusSettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @ModelAttribute("campusSettings")
    public Object campusSettings() {
        return settingsService.getSettings();
    }
}

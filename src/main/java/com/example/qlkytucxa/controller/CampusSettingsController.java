package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.entity.CampusSettings;
import com.example.qlkytucxa.service.CampusSettingsService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
@RequestMapping("/admin/settings/campus")
public class CampusSettingsController {
    private final CampusSettingsService settingsService;
    private final Path uploadRoot;

    public CampusSettingsController(CampusSettingsService settingsService,
                                    @Value("${app.upload.dir:src/main/resources/static/uploads}") String uploadDir) {
        this.settingsService = settingsService;
        this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    @GetMapping
    public String form(Model model) {
        CampusSettings settings = settingsService.getSettings();
        model.addAttribute("settings", new SettingsForm(settings.getInstitutionName(), settings.getBannerTitle(),
                settings.getBannerSubtitle(), settings.getBannerImage()));
        return "admin/campus-settings";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("settings") SettingsForm form, BindingResult result,
                       @RequestParam(value = "bannerFile", required = false) MultipartFile bannerFile,
                       Model model) throws IOException {
        if (form.institutionName() == null || form.institutionName().isBlank()) result.rejectValue("institutionName", "required", "Vui lòng nhập tên trường/cơ sở.");
        if (form.bannerTitle() == null || form.bannerTitle().isBlank()) result.rejectValue("bannerTitle", "required", "Vui lòng nhập tiêu đề banner.");
        if (form.bannerSubtitle() == null || form.bannerSubtitle().isBlank()) result.rejectValue("bannerSubtitle", "required", "Vui lòng nhập mô tả banner.");
        String imagePath = null;
        if (bannerFile != null && !bannerFile.isEmpty()) {
            String contentType = bannerFile.getContentType();
            if (!("image/png".equals(contentType) || "image/jpeg".equals(contentType) || "image/webp".equals(contentType))) {
                result.rejectValue("bannerTitle", "invalidBanner", "Ảnh banner phải có định dạng PNG, JPG hoặc WEBP.");
            } else if (bannerFile.getSize() > 5 * 1024 * 1024) {
                result.rejectValue("bannerTitle", "largeBanner", "Ảnh banner không được vượt quá 5 MB.");
            } else {
                String extension = switch (contentType) {
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    default -> ".jpg";
                };
                String fileName = UUID.randomUUID() + extension;
                Files.createDirectories(uploadRoot);
                Path target = uploadRoot.resolve(fileName).normalize();
                if (!target.startsWith(uploadRoot)) throw new IOException("Đường dẫn tệp không hợp lệ.");
                bannerFile.transferTo(target);
                imagePath = "/uploads/" + fileName;
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("currentImage", settingsService.getSettings().getBannerImage());
            return "admin/campus-settings";
        }
        CampusSettings current = settingsService.getSettings();
        settingsService.save(form.institutionName(), form.bannerTitle(), form.bannerSubtitle(),
                imagePath == null ? current.getBannerImage() : imagePath);
        return "redirect:/admin/settings/campus?saved";
    }

    public record SettingsForm(
            @NotBlank @Size(max = 150) String institutionName,
            @NotBlank @Size(max = 120) String bannerTitle,
            @NotBlank @Size(max = 255) String bannerSubtitle,
            String bannerImage) { }
}

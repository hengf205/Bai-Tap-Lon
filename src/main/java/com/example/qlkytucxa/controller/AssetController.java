package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.AssetDto;
import com.example.qlkytucxa.entity.Asset;
import com.example.qlkytucxa.entity.AssetCondition;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.repository.AssetRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/assets")
public class AssetController {
    private final AssetRepository assets;
    private final RoomRepository rooms;
    public AssetController(AssetRepository assets, RoomRepository rooms) { this.assets = assets; this.rooms = rooms; }
    @GetMapping public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("assetPage", assets.findAll(PageRequest.of(Math.max(page, 0), 15, Sort.by("assetName").ascending())));
        return "assets/list";
    }
    @GetMapping("/new") public String createForm(Model model) {
        AssetDto dto = new AssetDto(); dto.setQuantity(1); dto.setConditionStatus(AssetCondition.GOOD);
        model.addAttribute("asset", dto); prepare(model); return "assets/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Asset a = assets.findById(id).orElseThrow(() -> new IllegalArgumentException("Tài sản không tồn tại"));
        AssetDto dto = new AssetDto(); dto.setId(a.getId()); dto.setRoomId(a.getRoom().getId()); dto.setAssetName(a.getAssetName());
        dto.setQuantity(a.getQuantity()); dto.setConditionStatus(a.getConditionStatus()); dto.setPurchaseDate(a.getPurchaseDate()); dto.setNote(a.getNote());
        model.addAttribute("asset", dto); prepare(model); return "assets/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("asset") AssetDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepare(model); return "assets/form"; }
        Room room = rooms.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        Asset entity = Asset.builder().id(dto.getId()).room(room).assetName(dto.getAssetName().trim()).quantity(dto.getQuantity())
                .conditionStatus(dto.getConditionStatus()).purchaseDate(dto.getPurchaseDate()).note(dto.getNote()).build();
        assets.save(entity); return "redirect:/admin/assets";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { assets.deleteById(id); return "redirect:/admin/assets"; }
    private void prepare(Model model) { model.addAttribute("rooms", rooms.findAll()); model.addAttribute("conditions", AssetCondition.values()); }
}

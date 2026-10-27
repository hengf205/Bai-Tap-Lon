package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.Floor;
import com.example.qlkytucxa.repository.BuildingRepository;
import com.example.qlkytucxa.repository.FloorRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/floors")
public class FloorController {
    private final FloorRepository floors;
    private final BuildingRepository buildings;
    private final RoomRepository rooms;
    public FloorController(FloorRepository floors, BuildingRepository buildings, RoomRepository rooms) { this.floors = floors; this.buildings = buildings; this.rooms = rooms; }
    @GetMapping public String list(Model model) { model.addAttribute("floors", floors.findAll(Sort.by("building.buildingName").ascending().and(Sort.by("floorNumber")))); return "floors/list"; }
    @GetMapping("/new") public String createForm(Model model) { model.addAttribute("floor", new Floor()); prepare(model); return "floors/form"; }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) { model.addAttribute("floor", floors.findById(id).orElseThrow(() -> new IllegalArgumentException("Tầng không tồn tại"))); prepare(model); return "floors/form"; }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("floor") Floor floor, BindingResult result, Model model) {
        if (result.hasErrors()) { prepare(model); return "floors/form"; }
        Long buildingId = floor.getBuilding().getId();
        Building building = buildings.findById(buildingId).orElseThrow(() -> new IllegalArgumentException("Tòa nhà không tồn tại"));
        floor.setBuilding(building); floors.save(floor); return "redirect:/admin/floors";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) {
        if (rooms.countByFloorId(id) > 0) throw new IllegalStateException("Không thể xóa tầng đang có phòng");
        floors.deleteById(id); return "redirect:/admin/floors";
    }
    private void prepare(Model model) { model.addAttribute("buildings", buildings.findAll()); }
}

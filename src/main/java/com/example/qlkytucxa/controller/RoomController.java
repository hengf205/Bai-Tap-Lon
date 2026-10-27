package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.RoomDto;
import com.example.qlkytucxa.entity.Floor;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.entity.RoomType;
import com.example.qlkytucxa.repository.BuildingRepository;
import com.example.qlkytucxa.repository.FloorRepository;
import jakarta.validation.Valid;
import com.example.qlkytucxa.service.RoomService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/admin/rooms")
public class RoomController {
    private final RoomService service;
    private final BuildingRepository buildings;
    private final FloorRepository floors;
    public RoomController(RoomService service, BuildingRepository buildings, FloorRepository floors) { this.service = service; this.buildings = buildings; this.floors = floors; }
    @GetMapping public String list(@RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) Long buildingId,
                                  @RequestParam(required = false) RoomStatus status,
                                  @RequestParam(required = false) RoomType roomType,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("roomPage", service.search(keyword, buildingId, status, roomType,
                PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("roomCode").ascending())));
        model.addAttribute("keyword", keyword); model.addAttribute("buildingId", buildingId);
        model.addAttribute("selectedStatus", status); model.addAttribute("selectedRoomType", roomType);
        model.addAttribute("statuses", RoomStatus.values()); model.addAttribute("roomTypes", RoomType.values());
        model.addAttribute("buildings", buildings.findAll());
        return "rooms/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("room", RoomDto.builder().status(RoomStatus.AVAILABLE).capacity(1).build());
        prepareForm(model); return "rooms/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Room r = service.getById(id);
        model.addAttribute("room", RoomDto.builder().id(r.getId()).floorId(r.getFloor().getId()).roomCode(r.getRoomCode())
                .roomType(r.getRoomType()).capacity(r.getCapacity()).price(r.getPrice()).status(r.getStatus()).description(r.getDescription()).build());
        prepareForm(model); return "rooms/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("room") RoomDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { prepareForm(model); return "rooms/form"; }
        Floor floor = floors.findById(dto.getFloorId()).orElseThrow(() -> new IllegalArgumentException("Tầng không tồn tại"));
        Room entity = Room.builder().id(dto.getId()).floor(floor).roomCode(dto.getRoomCode().trim()).roomType(dto.getRoomType())
                .capacity(dto.getCapacity()).price(dto.getPrice()).status(dto.getStatus() == null ? RoomStatus.AVAILABLE : dto.getStatus()).description(dto.getDescription()).build();
        service.save(entity); return "redirect:/admin/rooms";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) { service.delete(id); return "redirect:/admin/rooms"; }
    private void prepareForm(Model model) {
        model.addAttribute("floors", floors.findAll());
        model.addAttribute("roomTypes", RoomType.values()); model.addAttribute("statuses", RoomStatus.values());
    }
}

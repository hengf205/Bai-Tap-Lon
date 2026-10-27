package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.TenantRegistrationDto;
import com.example.qlkytucxa.entity.BedStatus;
import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.BuildingStatus;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.RoomStatus;
import com.example.qlkytucxa.exception.DuplicateResourceException;
import com.example.qlkytucxa.exception.InvalidOperationException;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.BuildingRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.service.TenantRentalService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class AuthController {
    private final BuildingRepository buildings;
    private final RoomRepository rooms;
    private final BedRepository beds;
    private final TenantRentalService rentalService;

    public AuthController(BuildingRepository buildings, RoomRepository rooms, BedRepository beds,
                         TenantRentalService rentalService) {
        this.buildings = buildings;
        this.rooms = rooms;
        this.beds = beds;
        this.rentalService = rentalService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("registration", new TenantRegistrationDto());
        prepareRegistration(model);
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registration") TenantRegistrationDto form,
                           BindingResult result, Model model) {
        if (!result.hasErrors()) {
            try {
                rentalService.register(form);
                return "redirect:/login?registered";
            } catch (DuplicateResourceException | InvalidOperationException exception) {
                result.reject("registration.failed", exception.getMessage());
            }
        }
        prepareRegistration(model);
        return "auth/register";
    }

    @GetMapping("/register/rooms")
    @ResponseBody
    public List<RoomOption> availableRooms(@RequestParam Long buildingId) {
        Building building = buildings.findById(buildingId).orElse(null);
        if (building == null || building.getStatus() != BuildingStatus.ACTIVE) return List.of();
        return rooms.findByBuildingId(buildingId).stream()
                .map(room -> new RoomOption(room.getId(), room.getRoomCode(), room.getFloor().getFloorNumber(),
                        room.getPrice(), beds.countByRoomIdAndStatus(room.getId(), BedStatus.AVAILABLE)))
                .filter(room -> room.availableBeds() > 0)
                .toList();
    }

    @GetMapping("/register/beds")
    @ResponseBody
    public List<BedOption> availableBeds(@RequestParam Long roomId) {
        Room room = rooms.findByIdWithFloorAndBuilding(roomId).orElse(null);
        if (room == null || room.getFloor().getBuilding().getStatus() != BuildingStatus.ACTIVE
                || room.getStatus() == RoomStatus.MAINTENANCE) return List.of();
        return beds.findByRoomIdAndStatus(roomId, BedStatus.AVAILABLE).stream()
                .map(bed -> new BedOption(bed.getId(), bed.getBedCode())).toList();
    }

    private void prepareRegistration(Model model) {
        model.addAttribute("buildings", buildings.findByStatus(BuildingStatus.ACTIVE));
        model.addAttribute("genders", com.example.qlkytucxa.entity.Gender.values());
    }

    public record RoomOption(Long id, String roomCode, Integer floorNumber, BigDecimal monthlyPrice, long availableBeds) { }
    public record BedOption(Long id, String bedCode) { }
}

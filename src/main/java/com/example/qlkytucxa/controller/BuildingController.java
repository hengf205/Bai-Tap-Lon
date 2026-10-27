package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.BuildingDto;
import com.example.qlkytucxa.entity.Building;
import com.example.qlkytucxa.entity.BuildingStatus;
import com.example.qlkytucxa.entity.Bed;
import com.example.qlkytucxa.entity.Contract;
import com.example.qlkytucxa.entity.ContractStatus;
import com.example.qlkytucxa.entity.Floor;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.FloorRepository;
import com.example.qlkytucxa.service.BuildingService;
import jakarta.validation.Valid;
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/buildings")
public class BuildingController {
    private final BuildingService service;
    private final FloorRepository floors;
    private final BedRepository beds;
    private final ContractRepository contracts;
    public BuildingController(BuildingService service, FloorRepository floors, BedRepository beds, ContractRepository contracts) { this.service = service; this.floors = floors; this.beds = beds; this.contracts = contracts; }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) BuildingStatus status,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size, Model model) {
        model.addAttribute("buildingPage", service.search(keyword, status, PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("buildingName").ascending())));
        model.addAttribute("keyword", keyword); model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", BuildingStatus.values());
        return "buildings/list";
    }

    @GetMapping("/{id}") public String detail(@PathVariable Long id, Model model) {
        Building building = service.getById(id);
        List<Floor> floorList = floors.findByBuildingIdWithRooms(id);
        Map<Long, List<Bed>> bedsByRoom = new LinkedHashMap<>();
        Map<Long, String> tenantByBed = new LinkedHashMap<>();
        for (Floor floor : floorList) {
            for (Room room : floor.getRooms()) {
                List<Bed> roomBeds = beds.findByRoomId(room.getId());
                bedsByRoom.put(room.getId(), roomBeds);
                for (Contract contract : contracts.findByRoomIdOrderByStartDateDesc(room.getId())) {
                    if (contract.getStatus() == ContractStatus.ACTIVE && contract.getBed() != null) {
                        tenantByBed.putIfAbsent(contract.getBed().getId(), contract.getTenant().getFullName());
                    }
                }
            }
        }
        model.addAttribute("building", building); model.addAttribute("floors", floorList);
        model.addAttribute("bedsByRoom", bedsByRoom); model.addAttribute("tenantByBed", tenantByBed);
        return "buildings/detail";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("building", BuildingDto.builder().status(BuildingStatus.ACTIVE).numberOfFloors(1).build());
        model.addAttribute("statuses", BuildingStatus.values());
        return "buildings/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Building b = service.getById(id);
        model.addAttribute("building", BuildingDto.builder().id(b.getId()).buildingCode(b.getBuildingCode())
                .buildingName(b.getBuildingName()).address(b.getAddress()).numberOfFloors(b.getNumberOfFloors())
                .description(b.getDescription()).status(b.getStatus()).build());
        model.addAttribute("statuses", BuildingStatus.values());
        return "buildings/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("building") BuildingDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("statuses", BuildingStatus.values());
            return "buildings/form";
        }
        Building entity = Building.builder().id(dto.getId()).buildingCode(dto.getBuildingCode().trim())
                .buildingName(dto.getBuildingName().trim()).address(dto.getAddress()).numberOfFloors(dto.getNumberOfFloors())
                .description(dto.getDescription()).status(dto.getStatus() == null ? BuildingStatus.ACTIVE : dto.getStatus()).build();
        service.save(entity);
        return "redirect:/admin/buildings";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        service.delete(id);
        return "redirect:/admin/buildings";
    }
}

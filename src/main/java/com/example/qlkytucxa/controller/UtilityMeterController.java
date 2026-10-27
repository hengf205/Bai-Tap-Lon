package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.dto.UtilityMeterDto;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.entity.UtilityMeter;
import com.example.qlkytucxa.repository.RoomRepository;
import com.example.qlkytucxa.repository.UtilityMeterRepository;
import com.example.qlkytucxa.service.UtilityMeterService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/utilities")
public class UtilityMeterController {
    private final UtilityMeterService service;
    private final RoomRepository rooms;
    private final UtilityMeterRepository meters;
    public UtilityMeterController(UtilityMeterService service, RoomRepository rooms, UtilityMeterRepository meters) { this.service = service; this.rooms = rooms; this.meters = meters; }

    @GetMapping public String list(@RequestParam(required = false) Long roomId,
                                   @RequestParam(required = false) LocalDate fromDate,
                                   @RequestParam(required = false) LocalDate toDate,
                                   @RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("meterPage", meters.searchUtilityMeters(roomId, fromDate, toDate,
                PageRequest.of(Math.max(page, 0), 15, Sort.by("recordingDate").descending())));
        model.addAttribute("rooms", rooms.findAll()); model.addAttribute("roomId", roomId);
        model.addAttribute("fromDate", fromDate); model.addAttribute("toDate", toDate);
        return "utilities/list";
    }

    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("meter", UtilityMeterDto.builder().recordingDate(LocalDate.now())
                .electricityPrice(new java.math.BigDecimal("3500")).waterPrice(new java.math.BigDecimal("15000")).build());
        model.addAttribute("rooms", rooms.findAll()); return "utilities/form";
    }

    @PostMapping("/save") public String save(@Valid @ModelAttribute("meter") UtilityMeterDto dto, BindingResult result, Model model) {
        if (result.hasErrors()) { model.addAttribute("rooms", rooms.findAll()); return "utilities/form"; }
        Room room = rooms.findById(dto.getRoomId()).orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        UtilityMeter meter = UtilityMeter.builder().room(room).electricityOld(dto.getElectricityOld()).electricityNew(dto.getElectricityNew())
                .waterOld(dto.getWaterOld()).waterNew(dto.getWaterNew()).electricityPrice(dto.getElectricityPrice())
                .waterPrice(dto.getWaterPrice()).recordingDate(dto.getRecordingDate()).build();
        service.record(meter); return "redirect:/admin/utilities";
    }
}

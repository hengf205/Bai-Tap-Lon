package com.example.qlkytucxa.controller;

import com.example.qlkytucxa.entity.Bed;
import com.example.qlkytucxa.entity.BedStatus;
import com.example.qlkytucxa.entity.Room;
import com.example.qlkytucxa.repository.BedRepository;
import com.example.qlkytucxa.repository.ContractRepository;
import com.example.qlkytucxa.repository.RoomRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/beds")
public class BedController {
    private final BedRepository beds;
    private final RoomRepository rooms;
    private final ContractRepository contracts;
    public BedController(BedRepository beds, RoomRepository rooms, ContractRepository contracts) { this.beds = beds; this.rooms = rooms; this.contracts = contracts; }
    @GetMapping public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("bedPage", beds.findAll(PageRequest.of(Math.max(page, 0), 15, Sort.by("bedCode").ascending())));
        return "beds/list";
    }
    @GetMapping("/new") public String createForm(Model model) {
        model.addAttribute("bed", Bed.builder().status(BedStatus.AVAILABLE).build()); prepare(model); return "beds/form";
    }
    @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model model) {
        Bed bed = beds.findByIdWithRoom(id).orElseThrow(() -> new IllegalArgumentException("Giường không tồn tại"));
        model.addAttribute("bed", bed); prepare(model); return "beds/form";
    }
    @PostMapping("/save") public String save(@Valid @ModelAttribute("bed") Bed bed, BindingResult result, Model model) {
        if (result.hasErrors()) { prepare(model); return "beds/form"; }
        Room room = rooms.findById(bed.getRoom().getId()).orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        bed.setRoom(room); beds.save(bed); return "redirect:/admin/beds";
    }
    @PostMapping("/{id}/delete") public String delete(@PathVariable Long id) {
        if (!contracts.findByBedIdOrderByStartDateDesc(id).isEmpty()) throw new IllegalStateException("Không thể xóa giường đã có lịch sử hợp đồng");
        beds.deleteById(id); return "redirect:/admin/beds";
    }
    private void prepare(Model model) { model.addAttribute("rooms", rooms.findAll()); model.addAttribute("statuses", BedStatus.values()); }
}

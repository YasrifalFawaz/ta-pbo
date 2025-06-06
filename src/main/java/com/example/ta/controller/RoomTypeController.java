package com.example.ta.controller;

import com.example.ta.model.RoomType;
import com.example.ta.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/roomtype")
public class RoomTypeController {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    // Tampilkan list tipe kamar
    @GetMapping
    public String listRoomTypes(Model model) {
        model.addAttribute("listRoomTypes", roomTypeRepository.findAll());
        return "admin/roomtype-list";
    }

    // Form create
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("roomType", new RoomType());
        return "admin/roomtype-form";
    }

    // Form edit
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        RoomType roomType = roomTypeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Invalid room type Id:" + id));
        model.addAttribute("roomType", roomType);
        return "admin/roomtype-form";
    }

    // Proses simpan (create/edit)
    @PostMapping("/save")
    public String saveRoomType(@ModelAttribute RoomType roomType) {
        // Simpan roomType, JPA otomatis update jika ada id, insert jika id null
        roomTypeRepository.save(roomType);
        return "redirect:/admin/roomtype";
    }

    // Hapus tipe kamar
    @GetMapping("/delete/{id}")
    public String deleteRoomType(@PathVariable Long id) {
        roomTypeRepository.deleteById(id);
        return "redirect:/admin/roomtype";
    }
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("roomType", new RoomType());
        model.addAttribute("currentPage", "roomtype");
        return "admin/roomtype-form";
    }

}

package com.example.ta.controller;

import com.example.ta.model.Room;
import com.example.ta.model.RoomType;
import com.example.ta.repository.RoomRepository;
import com.example.ta.repository.RoomTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Controller
@RequestMapping("/admin/room")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    // List semua kamar
    @GetMapping
    public String listRooms(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("listRooms", rooms);
        model.addAttribute("currentPage", "room");
        return "admin/room-list";
    }

    // Tampilkan form tambah kamar
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("room", new Room());
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("currentPage", "room");
        return "admin/room-form"; 
    }

    // Tampilkan form edit kamar
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        model.addAttribute("room", room);
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("currentPage", "room"); 
        return "admin/room-form"; 
    }

    // Simpan data kamar (untuk tambah dan edit)
    @PostMapping("/save")
    public String saveRoom(@ModelAttribute Room room) {
        // Jika status belum diisi, default AVAILABLE
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus("AVAILABLE");
        }

        // Set waktu buat/update
        if (room.getId() == null) {
            room.setCreatedAt(java.time.LocalDateTime.now());
        }
        room.setUpdatedAt(java.time.LocalDateTime.now());

        // Set RoomType dari ID yang dikirim form
        if (room.getRoomType() != null && room.getRoomType().getId() != null) {
            RoomType rt = roomTypeRepository.findById(room.getRoomType().getId())
                .orElseThrow(() -> new IllegalArgumentException("Room Type tidak ditemukan"));
            room.setRoomType(rt);
        } else {
            room.setRoomType(null);
        }

        roomRepository.save(room);
        return "redirect:/admin/room";
    }

    // Hapus kamar berdasarkan ID
    @GetMapping("/delete/{id}")
    public String deleteRoom(@PathVariable Long id) {
        Room room = roomRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Room not found: " + id));
        roomRepository.delete(room);
        return "redirect:/admin/room";
    }
}
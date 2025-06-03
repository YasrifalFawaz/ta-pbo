package com.example.ta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import com.example.ta.model.User;
import com.example.ta.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // Dashboard
    @GetMapping("/dashboard")
    public String showDashboardPage(Model model) {
        model.addAttribute("currentPage", "dashboard");
        return "admin/dashboard";
    }

    // Reservasi
    @GetMapping("/reservasi")
    public String showReservasiPage(Model model) {
        model.addAttribute("currentPage", "reservasi");
        return "admin/reservasi";
    }

    // Kamar
    @GetMapping("/kamar")
    public String showKamarPage(Model model) {
        model.addAttribute("currentPage", "kamar");
        return "admin/kamar";
    }

    // List User (tampilkan tabel)
    @GetMapping("/user")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("listUsers", users);  // <- disesuaikan ke listUsers
        model.addAttribute("currentPage", "user");
        return "admin/user";
    }

    // Tambah User (form)
    @GetMapping("/user/create")
    public String showCreateForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("currentPage", "user");
        return "admin/create_user";
    }

    // Simpan User
    @PostMapping("/user/save")
    public String saveUser(@ModelAttribute User user) {
        userRepository.save(user);
        return "redirect:/admin/user";
    }

    // Edit User (form)
    @GetMapping("/user/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        model.addAttribute("user", user);
        model.addAttribute("currentPage", "user");
        return "admin/edit_user";
    }

    // Update User
    @PostMapping("/user/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user) {
        user.setId(id);
        userRepository.save(user);
        return "redirect:/admin/user";
    }

    // Hapus User
    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userRepository.deleteById(id);
        return "redirect:/admin/user";
    }
}

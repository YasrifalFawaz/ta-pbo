package com.example.ta.controller;

import com.example.ta.model.User;
import com.example.ta.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("user", new User());
        return "login";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute User user, Model model) {
        User u = userRepo.findByUsernameAndPassword(user.getUsername(), user.getPassword());
        if (u != null) {
            // Cek role user setelah login
            if ("ADMIN".equals(u.getRole())) {
                return "redirect:/admin/dashboard";
            } else if ("USER".equals(u.getRole())) {
                return "redirect:/user/index";
            } else {
                model.addAttribute("error", "Role tidak dikenali");
                return "login";
            }
        }
        model.addAttribute("error", "Username atau Password salah");
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user, Model model) {
        if (userRepo.findByUsername(user.getUsername()) != null) {
            model.addAttribute("error", "Username sudah digunakan");
            return "register";
        }

        user.setRole("USER");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepo.save(user);
        return "redirect:/login";
    }
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
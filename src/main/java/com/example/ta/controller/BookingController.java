package com.example.ta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import com.example.ta.model.Booking;
import com.example.ta.model.Room;
import com.example.ta.model.User;
import com.example.ta.repository.BookingRepository;
import com.example.ta.repository.RoomRepository;
import com.example.ta.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

    @Controller
    @RequestMapping("/admin/booking")
    public class BookingController {

        @Autowired
        private BookingRepository bookingRepository;
        
        @Autowired
        private RoomRepository roomRepository;

        @Autowired
        private UserRepository userRepository;

        // List All Bookings
        @GetMapping
        public String listBookings(Model model) {
            List<Booking> bookings = bookingRepository.findAll();
            model.addAttribute("listBookings", bookings);
            model.addAttribute("currentPage", "booking");
            return "admin/booking-list";
        }

        // Pending Bookings (perlu konfirmasi)
        @GetMapping("/pending")
        public String listPendingBookings(Model model) {
            List<Booking> pendingBookings = bookingRepository.findPendingBookings();
            model.addAttribute("listBookings", pendingBookings);
            model.addAttribute("bookingType", "pending");
            model.addAttribute("currentPage", "booking");
            return "admin/booking-list";
        }

        // Form Tambah Booking (untuk walk-in customer)
        @GetMapping("/create")
        public String showCreateForm(Model model) {
            List<User> users = userRepository.findAll();
            List<Room> availableRooms = roomRepository.findByStatus("AVAILABLE");

            System.out.println("Available rooms: " + availableRooms.size());
            availableRooms.forEach(r -> System.out.println(r.getRoomNumber()));

            model.addAttribute("booking", new Booking());
            model.addAttribute("users", users);
            model.addAttribute("rooms", availableRooms);
            model.addAttribute("currentPage", "booking");
            return "admin/booking-form";
        }

        // Simpan Booking
        @PostMapping("/save")
        public String saveBooking(@ModelAttribute Booking booking,
                                BindingResult bindingResult,
                                Model model) {
            
            // Cek jika ada error validasi
            if (bindingResult.hasErrors()) {
                // Tampilkan error di console untuk debugging
                bindingResult.getAllErrors().forEach(error -> {
                    System.out.println("Error validasi: " + error.toString());
                });
                
                // Kirim kembali data yang diperlukan ke form
                model.addAttribute("users", userRepository.findAll());
                model.addAttribute("rooms", roomRepository.findByStatus("AVAILABLE"));
                model.addAttribute("currentPage", "booking");
                return "admin/booking-form";
            }

            try {
                // Ambil user dan room dari database
                User user = userRepository.findById(booking.getUser().getId())
                    .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
                Room room = roomRepository.findById(booking.getRoom().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Kamar tidak ditemukan"));

                // Set relasi
                booking.setUser(user);
                booking.setRoom(room);
                
                // Hitung nilai-nilai
                booking.calculateTotalNights();
                booking.calculateTotalPrice();
                booking.generateBookingCode();
                booking.updateTimestamp();

                // Simpan booking
                bookingRepository.save(booking);
                return "redirect:/admin/booking";
                
            } catch (Exception e) {
                // Tangani error lainnya
                model.addAttribute("error", "Gagal menyimpan booking: " + e.getMessage());
                model.addAttribute("users", userRepository.findAll());
                model.addAttribute("rooms", roomRepository.findByStatus("AVAILABLE"));
                model.addAttribute("currentPage", "booking");
                return "admin/booking";
            }
        }

        // Detail Booking
        @GetMapping("/detail/{id}")
        public String showBookingDetail(@PathVariable Long id, Model model) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
            model.addAttribute("booking", booking);
            model.addAttribute("currentPage", "booking");
            return "admin/booking-detail";
        }

        // Konfirmasi Booking
        @PostMapping("/confirm/{id}")
        public String confirmBooking(@PathVariable Long id) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
            
            booking.setStatus("CONFIRMED");
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
            
            return "redirect:/admin/booking";
        }

        // Batalkan Booking
        @PostMapping("/cancel/{id}")
        public String cancelBooking(@PathVariable Long id) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
            
            booking.setStatus("CANCELLED");
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
            
            // Set room back to available
            Room room = booking.getRoom();
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
            
            return "redirect:/admin/booking";
        }

        // Check-in
        @PostMapping("/checkin/{id}")
        public String checkIn(@PathVariable Long id) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
            
            booking.setStatus("CHECKED_IN");
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
            
            // Update room status
            Room room = booking.getRoom();
            room.setStatus("OCCUPIED");
            roomRepository.save(room);
            
            return "redirect:/admin/booking";
        }

        // Check-out
        @PostMapping("/checkout/{id}")
        public String checkOut(@PathVariable Long id) {
            Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
            
            booking.setStatus("CHECKED_OUT");
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
            
            // Set room back to available
            Room room = booking.getRoom();
            room.setStatus("AVAILABLE");
            roomRepository.save(room);
            
            return "redirect:/admin/booking";
        }

        // Today's Check-ins
        @GetMapping("/checkin-today")
        public String todayCheckIns(Model model) {
            List<Booking> todayCheckIns = bookingRepository.findCheckInsForToday(LocalDate.now());
            model.addAttribute("listBookings", todayCheckIns);
            model.addAttribute("bookingType", "checkin-today");
            model.addAttribute("currentPage", "booking");
            return "admin/booking-list";
        }

        // Today's Check-outs
        @GetMapping("/checkout-today")
        public String todayCheckOuts(Model model) {
            List<Booking> todayCheckOuts = bookingRepository.findCheckOutsForToday(LocalDate.now());
            model.addAttribute("listBookings", todayCheckOuts);
            model.addAttribute("bookingType", "checkout-today");
            model.addAttribute("currentPage", "booking");
            return "admin/booking-list";
        }

        // Filter by status
        @GetMapping("/status/{status}")
        public String listBookingsByStatus(@PathVariable String status, Model model) {
            List<Booking> bookings = bookingRepository.findByStatusOrderByCreatedAtDesc(status);
            model.addAttribute("listBookings", bookings);
            model.addAttribute("selectedStatus", status);
            model.addAttribute("currentPage", "booking");
            return "admin/booking-list";
        }
        @GetMapping("/test-rooms")
    @ResponseBody
    public List<Room> testRooms() {
        return roomRepository.findByStatus("AVAILABLE");
    }

    }
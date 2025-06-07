package com.example.ta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDate;
import java.util.List;
import com.example.ta.model.Booking;
import com.example.ta.model.Room;
import com.example.ta.model.User;
import com.example.ta.repository.BookingRepository;
import com.example.ta.repository.RoomRepository;
import com.example.ta.repository.UserRepository;

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

    // Simpan Booking - DIPERBAIKI
    @PostMapping("/save")
    public String saveBooking(@ModelAttribute Booking booking, 
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             @RequestParam("user.id") Long userId,
                             @RequestParam("room.id") Long roomId,
                             @RequestParam("checkInDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkInDate,
                             @RequestParam("checkOutDate") @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOutDate,
                             @RequestParam(value = "guestCount", required = false, defaultValue = "1") Integer guestCount,
                             @RequestParam(value = "specialRequests", required = false) String specialRequests) {
        
        try {
            System.out.println("=== DEBUG BOOKING SAVE ===");
            System.out.println("User ID: " + userId);
            System.out.println("Room ID: " + roomId);
            System.out.println("Check-in Date: " + checkInDate);
            System.out.println("Check-out Date: " + checkOutDate);
            System.out.println("Guest Count: " + guestCount);
            System.out.println("Special Requests: " + specialRequests);
            
            // Validasi input
            if (userId == null || roomId == null) {
                redirectAttributes.addFlashAttribute("error", "User dan Room harus dipilih");
                return "redirect:/admin/booking/create";
            }
            
            if (checkInDate == null || checkOutDate == null) {
                redirectAttributes.addFlashAttribute("error", "Tanggal check-in dan check-out harus diisi");
                return "redirect:/admin/booking/create";
            }
            
            if (checkInDate.isAfter(checkOutDate) || checkInDate.isEqual(checkOutDate)) {
                redirectAttributes.addFlashAttribute("error", "Tanggal check-out harus setelah tanggal check-in");
                return "redirect:/admin/booking/create";
            }
            
            // Cari User dan Room
            User user = userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan dengan ID: " + userId));
            Room room = roomRepository.findById(roomId)
                        .orElseThrow(() -> new IllegalArgumentException("Room tidak ditemukan dengan ID: " + roomId));

            // Set data booking
            booking.setUser(user);
            booking.setRoom(room);
            booking.setCheckInDate(checkInDate);
            booking.setCheckOutDate(checkOutDate);
            booking.setGuestCount(guestCount);
            booking.setSpecialRequests(specialRequests);

            // Set status default jika kosong
            if (booking.getStatus() == null || booking.getStatus().isEmpty()) {
                booking.setStatus("CONFIRMED");
            }

            // Hitung total malam dan harga
            booking.calculateTotalNights();
            booking.calculateTotalPrice();
            booking.generateBookingCode();

            // Set timestamps
            if (booking.getId() == null) {
                booking.setCreatedAt(java.time.LocalDateTime.now());
            }
            booking.setUpdatedAt(java.time.LocalDateTime.now());

            // Simpan booking
            Booking savedBooking = bookingRepository.save(booking);
            System.out.println("Booking saved with ID: " + savedBooking.getId());

            // Update status room jika check-in hari ini
            if (checkInDate.equals(LocalDate.now())) {
                room.setStatus("OCCUPIED");
                roomRepository.save(room);
                System.out.println("Room status updated to OCCUPIED");
            }

            redirectAttributes.addFlashAttribute("success", "Booking berhasil disimpan");
            return "redirect:/admin/booking";
            
        } catch (Exception e) {
            System.err.println("Error saving booking: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Terjadi kesalahan: " + e.getMessage());
            return "redirect:/admin/booking/create";
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
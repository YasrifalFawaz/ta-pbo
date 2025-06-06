package com.example.ta.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import com.example.ta.model.Booking;
import com.example.ta.model.Room;
import com.example.ta.model.RoomType;
import com.example.ta.model.User;
import com.example.ta.repository.BookingRepository;
import com.example.ta.repository.RoomRepository;
import com.example.ta.repository.RoomTypeRepository;
import com.example.ta.repository.UserRepository;

@Controller
@RequestMapping("/user")
public class UserBookingController {

    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private UserRepository userRepository;

    // Home page - tampilkan room types
    @GetMapping("/home")
    public String home(Model model) {
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        model.addAttribute("roomTypes", roomTypes);
        return "user/home";
    }

    // Search available rooms
    @GetMapping("/search")
    public String searchRooms(@RequestParam(required = false) LocalDate checkIn,
                             @RequestParam(required = false) LocalDate checkOut,
                             @RequestParam(required = false) Long roomTypeId,
                             Model model) {
        
        List<Room> availableRooms;
        
        if (checkIn != null && checkOut != null) {
            if (roomTypeId != null) {
                RoomType roomType = roomTypeRepository.findById(roomTypeId).orElse(null);
                availableRooms = roomRepository.findAvailableRoomsByTypeAndDateRange(roomType, checkIn, checkOut);
            } else {
                availableRooms = roomRepository.findAvailableRoomsForDateRange(checkIn, checkOut);
            }
        } else {
            availableRooms = roomRepository.findByStatus("AVAILABLE");
        }
        
        List<RoomType> roomTypes = roomTypeRepository.findAll();
        
        model.addAttribute("rooms", availableRooms);
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("checkIn", checkIn);
        model.addAttribute("checkOut", checkOut);
        model.addAttribute("selectedRoomTypeId", roomTypeId);
        return "user/search-rooms";
    }

    // Booking form
    @GetMapping("/booking/{roomId}")
    public String showBookingForm(@PathVariable Long roomId,
                                 @RequestParam(required = false) LocalDate checkIn,
                                 @RequestParam(required = false) LocalDate checkOut,
                                 Model model) {
        
        Room room = roomRepository.findById(roomId)
            .orElseThrow(() -> new IllegalArgumentException("Room tidak ditemukan"));
        
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCheckInDate(checkIn);
        booking.setCheckOutDate(checkOut);
        
        if (checkIn != null && checkOut != null) {
            booking.calculateTotalNights();
            booking.calculateTotalPrice();
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("room", room);
        return "user/booking-form";
    }

    // Submit booking
    @PostMapping("/booking/submit")
    public String submitBooking(@ModelAttribute Booking booking,
                               @RequestParam Long userId) {
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        
        booking.setUser(user);
        booking.setStatus("PENDING");
        booking.calculateTotalNights();
        booking.calculateTotalPrice();
        booking.generateBookingCode();
        booking.setCreatedAt(java.time.LocalDateTime.now());
        booking.setUpdatedAt(java.time.LocalDateTime.now());
        
        bookingRepository.save(booking);
        
        return "redirect:/user/my-bookings/" + userId;
    }

    // My bookings
    @GetMapping("/my-bookings/{userId}")
    public String myBookings(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        
        List<Booking> myBookings = bookingRepository.findByUserOrderByCreatedAtDesc(user);
        
        model.addAttribute("bookings", myBookings);
        model.addAttribute("user", user);
        return "user/my-bookings";
    }

    // Booking detail
    @GetMapping("/booking-detail/{bookingId}")
    public String bookingDetail(@PathVariable Long bookingId, Model model) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
        
        model.addAttribute("booking", booking);
        return "user/booking-detail";
    }

    // Cancel booking (if still pending)
    @PostMapping("/booking/cancel/{bookingId}")
    public String cancelBooking(@PathVariable Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new IllegalArgumentException("Booking tidak ditemukan"));
        
        if ("PENDING".equals(booking.getStatus())) {
            booking.setStatus("CANCELLED");
            booking.setUpdatedAt(java.time.LocalDateTime.now());
            bookingRepository.save(booking);
        }
        
        return "redirect:/user/my-bookings/" + booking.getUser().getId();
    }
}
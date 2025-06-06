package com.example.ta.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String bookingCode; 
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer totalNights;
    private BigDecimal totalPrice;
    private String status; 
    private String specialRequests;
    private Integer guestCount;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Many-to-One relationship with Room
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Booking(User user, Room room, LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount) {
        this();
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
        this.status = "PENDING";
        this.calculateTotalNights();
        this.calculateTotalPrice();
        this.generateBookingCode();
    }
    
    // Business Methods
    public void calculateTotalNights() {
        if (checkInDate != null && checkOutDate != null) {
            this.totalNights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        }
    }
    
    public void calculateTotalPrice() {
        if (room != null && totalNights != null && totalNights > 0) {
            this.totalPrice = room.getPrice().multiply(BigDecimal.valueOf(totalNights));
        }
    }
    
    public void generateBookingCode() {
        // Simple booking code generation - you can improve this
        this.bookingCode = "BK" + System.currentTimeMillis();
    }
    
    // Method to update timestamps
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingCode() { return bookingCode; }
    public void setBookingCode(String bookingCode) { this.bookingCode = bookingCode; }
    
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { 
        this.checkInDate = checkInDate;
        calculateTotalNights();
        calculateTotalPrice();
        updateTimestamp();
    }
    
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { 
        this.checkOutDate = checkOutDate;
        calculateTotalNights();
        calculateTotalPrice();
        updateTimestamp();
    }
    
    public Integer getTotalNights() { return totalNights; }
    public void setTotalNights(Integer totalNights) { this.totalNights = totalNights; }
    
    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        updateTimestamp();
    }
    
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    
    public Integer getGuestCount() { return guestCount; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { 
        this.room = room;
        calculateTotalPrice();
        updateTimestamp();
    }
}

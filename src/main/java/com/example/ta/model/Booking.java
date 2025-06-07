package com.example.ta.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true)
    private String bookingCode; 
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;
    
    @Column(name = "total_nights")
    private Integer totalNights;
    
    @Column(name = "total_price", precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    @Column(length = 50, nullable = false)
    private String status; 
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "guest_count", nullable = false)
    private Integer guestCount;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Many-to-One relationship with Room
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    // Constructors
    public Booking() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "PENDING";
        this.guestCount = 1;
    }

    public Booking(User user, Room room, LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount) {
        this();
        this.user = user;
        this.room = room;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount != null ? guestCount : 1;
        this.calculateTotalNights();
        this.calculateTotalPrice();
        this.generateBookingCode();
    }

    // Business Methods
    public void calculateTotalNights() {
        if (checkInDate != null && checkOutDate != null) {
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            this.totalNights = (int) Math.max(nights, 1); // Minimum 1 malam
        }
    }

    public void calculateTotalPrice() {
        if (room != null && room.getPrice() != null && totalNights != null && totalNights > 0) {
            this.totalPrice = room.getPrice().multiply(BigDecimal.valueOf(totalNights));
        } else {
            this.totalPrice = BigDecimal.ZERO;
        }
    }

    public void generateBookingCode() {
        if (this.bookingCode == null || this.bookingCode.isEmpty()) {
            // Format: BK + timestamp + random 3 digit
            long timestamp = System.currentTimeMillis();
            int random = (int) (Math.random() * 1000);
            this.bookingCode = String.format("BK%d%03d", timestamp, random);
        }
    }

    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
    
    // PrePersist and PreUpdate callbacks
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null || this.status.isEmpty()) {
            this.status = "PENDING";
        }
        if (this.guestCount == null) {
            this.guestCount = 1;
        }
        generateBookingCode();
        calculateTotalNights();
        calculateTotalPrice();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
        calculateTotalNights();
        calculateTotalPrice();
    }
    
    // Validation method
    public boolean isValid() {
        return user != null && 
               room != null && 
               checkInDate != null && 
               checkOutDate != null && 
               checkInDate.isBefore(checkOutDate) &&
               guestCount != null && 
               guestCount > 0;
    }
    
    // Getters and Setters
    public Long getId() { 
        return id; 
    }
    
    public void setId(Long id) { 
        this.id = id; 
    }
    
    public String getBookingCode() { 
        return bookingCode; 
    }
    
    public void setBookingCode(String bookingCode) { 
        this.bookingCode = bookingCode; 
    }
    
    public LocalDate getCheckInDate() { 
        return checkInDate; 
    }
    
    public void setCheckInDate(LocalDate checkInDate) { 
        this.checkInDate = checkInDate;
        if (this.checkOutDate != null) {
            calculateTotalNights();
            calculateTotalPrice();
        }
        updateTimestamp();
    }
    
    public LocalDate getCheckOutDate() { 
        return checkOutDate; 
    }
    
    public void setCheckOutDate(LocalDate checkOutDate) { 
        this.checkOutDate = checkOutDate;
        if (this.checkInDate != null) {
            calculateTotalNights();
            calculateTotalPrice();
        }
        updateTimestamp();
    }
    
    public Integer getTotalNights() { 
        return totalNights; 
    }
    
    public void setTotalNights(Integer totalNights) { 
        this.totalNights = totalNights; 
    }
    
    public BigDecimal getTotalPrice() { 
        return totalPrice != null ? totalPrice : BigDecimal.ZERO; 
    }
    
    public void setTotalPrice(BigDecimal totalPrice) { 
        this.totalPrice = totalPrice; 
    }
    
    public String getStatus() { 
        return status; 
    }
    
    public void setStatus(String status) { 
        this.status = status;
        updateTimestamp();
    }
    
    public String getSpecialRequests() { 
        return specialRequests; 
    }
    
    public void setSpecialRequests(String specialRequests) { 
        this.specialRequests = specialRequests; 
    }
    
    public Integer getGuestCount() { 
        return guestCount != null ? guestCount : 1; 
    }
    
    public void setGuestCount(Integer guestCount) { 
        this.guestCount = guestCount != null ? guestCount : 1; 
    }
    
    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }
    
    public User getUser() { 
        return user; 
    }
    
    public void setUser(User user) { 
        this.user = user;
        updateTimestamp();
    }
    
    public Room getRoom() { 
        return room; 
    }
    
    public void setRoom(Room room) { 
        this.room = room;
        if (this.totalNights != null) {
            calculateTotalPrice();
        }
        updateTimestamp();
    }
    
    // ToString for debugging
    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", bookingCode='" + bookingCode + '\'' +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", totalNights=" + totalNights +
                ", totalPrice=" + totalPrice +
                ", status='" + status + '\'' +
                ", guestCount=" + guestCount +
                ", userId=" + (user != null ? user.getId() : null) +
                ", roomId=" + (room != null ? room.getId() : null) +
                '}';
    }
}
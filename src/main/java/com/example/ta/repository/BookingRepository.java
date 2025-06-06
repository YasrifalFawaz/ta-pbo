package com.example.ta.repository;

import com.example.ta.model.Booking;
import com.example.ta.model.Room;
import com.example.ta.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    // Find by booking code
    Optional<Booking> findByBookingCode(String bookingCode);
    
    // Find by user
    List<Booking> findByUser(User user);
    List<Booking> findByUserOrderByCreatedAtDesc(User user);
    
    // Find by room
    List<Booking> findByRoom(Room room);
    List<Booking> findByRoomOrderByCheckInDateAsc(Room room);
    
    // Find by status
    List<Booking> findByStatus(String status);
    List<Booking> findByStatusOrderByCreatedAtDesc(String status);
    
    // Find by user and status
    List<Booking> findByUserAndStatus(User user, String status);
    
    // Find by date range
    List<Booking> findByCheckInDateBetween(LocalDate startDate, LocalDate endDate);
    List<Booking> findByCheckOutDateBetween(LocalDate startDate, LocalDate endDate);
    
    // Find bookings for today
    @Query("SELECT b FROM Booking b WHERE b.checkInDate = :today AND b.status = 'CONFIRMED'")
    List<Booking> findCheckInsForToday(@Param("today") LocalDate today);
    
    @Query("SELECT b FROM Booking b WHERE b.checkOutDate = :today AND b.status = 'CHECKED_IN'")
    List<Booking> findCheckOutsForToday(@Param("today") LocalDate today);
    
    // Find overlapping bookings for a room
    @Query("SELECT b FROM Booking b WHERE b.room = :room AND " +
           "(b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn) AND " +
           "b.status NOT IN ('CANCELLED', 'CHECKED_OUT')")
    List<Booking> findOverlappingBookings(@Param("room") Room room,
                                        @Param("checkIn") LocalDate checkIn,
                                        @Param("checkOut") LocalDate checkOut);
    
    // Find pending bookings
    @Query("SELECT b FROM Booking b WHERE b.status = 'PENDING' ORDER BY b.createdAt ASC")
    List<Booking> findPendingBookings();
    
    // Find bookings created in date range
    List<Booking> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find recent bookings
    @Query("SELECT b FROM Booking b ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookings();
    
    // Statistics queries
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status")
    Long countByStatus(@Param("status") String status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.checkInDate = :date")
    Long countBookingsForDate(@Param("date") LocalDate date);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.createdAt >= :startDate AND b.createdAt <= :endDate")
    Long countBookingsInDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    // Monthly statistics
    @Query("SELECT COUNT(b) FROM Booking b WHERE YEAR(b.createdAt) = :year AND MONTH(b.createdAt) = :month")
    Long countBookingsInMonth(@Param("year") int year, @Param("month") int month);
    
    // Revenue calculation
    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.status = 'CHECKED_OUT' AND " +
           "b.checkOutDate >= :startDate AND b.checkOutDate <= :endDate")
    Double calculateRevenueInDateRange(@Param("startDate") LocalDate startDate, 
                                     @Param("endDate") LocalDate endDate);
    
    // Find bookings by user ID (for admin view)
    @Query("SELECT b FROM Booking b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    List<Booking> findByUserId(@Param("userId") Long userId);
    
    // Check room availability
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN false ELSE true END FROM Booking b WHERE " +
           "b.room = :room AND " +
           "(b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn) AND " +
           "b.status NOT IN ('CANCELLED', 'CHECKED_OUT')")
    boolean isRoomAvailable(@Param("room") Room room,
                          @Param("checkIn") LocalDate checkIn,
                          @Param("checkOut") LocalDate checkOut);
}
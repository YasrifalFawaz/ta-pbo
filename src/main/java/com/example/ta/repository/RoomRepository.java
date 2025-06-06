package com.example.ta.repository;

import com.example.ta.model.Room;
import com.example.ta.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Find by room number
    Optional<Room> findByRoomNumber(String roomNumber);
    
    // Check if room number exists
    boolean existsByRoomNumber(String roomNumber);
    
    // Find by status
    List<Room> findByStatus(String status);
    
    // Find available rooms
    List<Room> findByStatusOrderByRoomNumberAsc(String status);
    
    // Find by room type
    List<Room> findByRoomType(RoomType roomType);
    List<Room> findByRoomTypeAndStatus(RoomType roomType, String status);
    
    // Find by floor
    List<Room> findByFloor(Integer floor);
    List<Room> findByFloorAndStatus(Integer floor, String status);
    
    // Find by price range
    List<Room> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Room> findByPriceLessThanEqual(BigDecimal maxPrice);
    List<Room> findByPriceGreaterThanEqual(BigDecimal minPrice);
    
    // Find available rooms by room type
    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.status = 'AVAILABLE'")
    List<Room> findAvailableRoomsByType(@Param("roomType") RoomType roomType);
    
    // Find available rooms not booked in date range
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "(b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn) AND " +
           "b.status NOT IN ('CANCELLED', 'CHECKED_OUT'))")
    List<Room> findAvailableRoomsForDateRange(@Param("checkIn") LocalDate checkIn, 
                                             @Param("checkOut") LocalDate checkOut);
    
    // Find available rooms by type and date range
    @Query("SELECT r FROM Room r WHERE r.roomType = :roomType AND r.status = 'AVAILABLE' AND r.id NOT IN " +
           "(SELECT b.room.id FROM Booking b WHERE " +
           "(b.checkInDate <= :checkOut AND b.checkOutDate >= :checkIn) AND " +
           "b.status NOT IN ('CANCELLED', 'CHECKED_OUT'))")
    List<Room> findAvailableRoomsByTypeAndDateRange(@Param("roomType") RoomType roomType,
                                                   @Param("checkIn") LocalDate checkIn, 
                                                   @Param("checkOut") LocalDate checkOut);
    
    // Count rooms by status
    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = :status")
    Long countByStatus(@Param("status") String status);
    
    // Count available rooms
    @Query("SELECT COUNT(r) FROM Room r WHERE r.status = 'AVAILABLE'")
    Long countAvailableRooms();
}
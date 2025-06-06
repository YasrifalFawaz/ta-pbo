package com.example.ta.repository;

import com.example.ta.model.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    
    // Find by type name
    Optional<RoomType> findByTypeName(String typeName);
    
    // Find by type name (case insensitive)
    Optional<RoomType> findByTypeNameIgnoreCase(String typeName);
    
    // Check if type name exists
    boolean existsByTypeName(String typeName);
    
    // Find all ordered by price
    List<RoomType> findAllByOrderByBasePriceAsc();
    List<RoomType> findAllByOrderByBasePriceDesc();
    
    // Find by max occupancy
    List<RoomType> findByMaxOccupancyGreaterThanEqual(Integer occupancy);
    
    // Custom query to find room types with available rooms
    @Query("SELECT DISTINCT rt FROM RoomType rt JOIN rt.rooms r WHERE r.status = 'AVAILABLE'")
    List<RoomType> findRoomTypesWithAvailableRooms();
}
package com.example.ta.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
public class RoomType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String typeName;
    private String description;
    private BigDecimal basePrice;
    private Integer maxOccupancy;
    private String amenities;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // One-to-Many relationship with Room
    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL)
    private List<Room> rooms;
    
    // Constructors
    public RoomType() {}
    
    public RoomType(String typeName, String description, BigDecimal basePrice, Integer maxOccupancy, String amenities) {
        this.typeName = typeName;
        this.description = description;
        this.basePrice = basePrice;
        this.maxOccupancy = maxOccupancy;
        this.amenities = amenities;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
     // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
    
    public Integer getMaxOccupancy() { return maxOccupancy; }
    public void setMaxOccupancy(Integer maxOccupancy) { this.maxOccupancy = maxOccupancy; }
    
    public String getAmenities() { return amenities; }
    public void setAmenities(String amenities) { this.amenities = amenities; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public List<Room> getRooms() { return rooms; }
    public void setRooms(List<Room> rooms) { this.rooms = rooms; }
}
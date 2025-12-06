package com.viewpulse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
// FIX: Changed table name from plural to singular to match SQL script
@Table(name = "system_admin_owner")
public class SystemAdminOwner {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "system_admin_id", nullable = false)
    private Long systemAdminId; 
    
    @Column(name = "owner_id", nullable = false, unique = true)
    private Long ownerId; 
    
    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;
    
    // Constructors
    public SystemAdminOwner() {
        this.assignedAt = LocalDateTime.now();
    }
    
    public SystemAdminOwner(Long systemAdminId, Long ownerId) {
        this.systemAdminId = systemAdminId;
        this.ownerId = ownerId;
        this.assignedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getSystemAdminId() {
        return systemAdminId;
    }
    
    public void setSystemAdminId(Long systemAdminId) {
        this.systemAdminId = systemAdminId;
    }
    
    public Long getOwnerId() {
        return ownerId;
    }
    
    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }
    
    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
    
    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }
}
package com.viewpulse.repository;

import com.viewpulse.model.SystemAdminOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SystemAdminOwnerRepository extends JpaRepository<SystemAdminOwner, Long> {
    
    // Find all owners under a specific system admin (CHANGED to Long)
    List<SystemAdminOwner> findBySystemAdminId(Long systemAdminId);
    
    // Find which system admin owns a specific owner (CHANGED to Long)
    Optional<SystemAdminOwner> findByOwnerId(Long ownerId);
    
    // Check if owner is already assigned (CHANGED to Long)
    boolean existsByOwnerId(Long ownerId);
    
    // Delete assignment by owner ID (CHANGED to Long)
    void deleteByOwnerId(Long ownerId);
}
package com.viewpulse.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.viewpulse.model.AdminUser;
import com.viewpulse.model.Device;
import com.viewpulse.model.SystemAdminOwner;
import com.viewpulse.repository.AdminUserRepository;
import com.viewpulse.repository.DeviceRepository;
import com.viewpulse.repository.SystemAdminOwnerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class HierarchyService {

@Autowired
private PasswordEncoder passwordEncoder;
    
    @Autowired
    private AdminUserRepository adminUserRepository;
    
    @Autowired
    private SystemAdminOwnerRepository systemAdminOwnerRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    // Get all system admins (for super admin)
    public List<Map<String, Object>> getAllSystemAdmins() {
        List<AdminUser> systemAdmins = adminUserRepository.findByRole("system_admin");
        
        return systemAdmins.stream().map(admin -> {
            Map<String, Object> map = new HashMap<>();
            map.put("admin_id", admin.getAdminId());
            map.put("username", admin.getUsername());
            map.put("role", admin.getRole());
            map.put("is_active", admin.getIsActive());
            map.put("created_at", admin.getCreatedAt());
            
            // Fix line 44: use Long for findBySystemAdminId argument
            List<SystemAdminOwner> assignments = systemAdminOwnerRepository.findBySystemAdminId(admin.getAdminId());
            map.put("owner_count", assignments.size());
            
            return map;
        }).collect(Collectors.toList());
    }
    
    // Get all owners under a specific system admin (Changed method signature to Long)
    public List<Map<String, Object>> getOwnersUnderSystemAdmin(Long systemAdminId) {
        // Fix line 53: systemAdminId is already Long
        List<SystemAdminOwner> assignments = systemAdminOwnerRepository.findBySystemAdminId(systemAdminId);
        
        return assignments.stream().map(assignment -> {
            // Fix line 58: ownerId is now Long
            Optional<AdminUser> ownerOpt = adminUserRepository.findById(assignment.getOwnerId());
            if (ownerOpt.isPresent()) {
                AdminUser owner = ownerOpt.get();
                Map<String, Object> map = new HashMap<>();
                map.put("admin_id", owner.getAdminId());
                map.put("username", owner.getUsername());
                map.put("role", owner.getRole());
                map.put("location_id", owner.getLocationId());
                map.put("is_active", owner.getIsActive());
                map.put("created_at", owner.getCreatedAt());
                map.put("assigned_at", assignment.getAssignedAt());
                
                // FindByLocationId expects Integer, which LocationId is
                if (owner.getLocationId() != null) {
                    List<Device> devices = deviceRepository.findByLocationId(owner.getLocationId());
                    map.put("device_count", devices.size());
                } else {
                    map.put("device_count", 0);
                }
                
                return map;
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }
    
    // Get all devices under a specific owner (Changed method signature to Long)
    public List<Map<String, Object>> getDevicesUnderOwner(Long ownerId) {
        Optional<AdminUser> ownerOpt = adminUserRepository.findById(ownerId);
        
        if (ownerOpt.isEmpty() || ownerOpt.get().getLocationId() == null) {
            return new ArrayList<>();
        }
        
        // locationId is Integer
        Integer locationId = ownerOpt.get().getLocationId();
        List<Device> devices = deviceRepository.findByLocationId(locationId);
        
        return devices.stream().map(device -> {
            Map<String, Object> map = new HashMap<>();
            map.put("device_id", device.getDeviceId());
            map.put("device_code", device.getDeviceCode());
            map.put("location_id", device.getLocationId());
            map.put("is_logged_in", device.getIsLoggedIn());
            map.put("created_at", device.getCreatedAt());
            return map;
        }).collect(Collectors.toList());
    }
    
    // Create system admin (by super admin)
    @Transactional
    public Map<String, Object> createSystemAdmin(String username, String password, Integer createdBy) {
        Map<String, Object> response = new HashMap<>();
        
        if (adminUserRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }
        
        AdminUser systemAdmin = new AdminUser();
        systemAdmin.setUsername(username);
        systemAdmin.setPassword(passwordEncoder.encode(password)); 
        systemAdmin.setRole("system_admin");
        systemAdmin.setIsActive(true);
        
        systemAdmin.setCreatedBy(createdBy);
        
        AdminUser saved = adminUserRepository.save(systemAdmin);
        
        response.put("success", true);
        response.put("message", "System admin created successfully");
        response.put("admin_id", saved.getAdminId());
        response.put("username", saved.getUsername());
        
        return response;
    }
    
    // Create owner and assign to system admin
    @Transactional
    public Map<String, Object> createOwner(String username, String password, Integer locationId, 
                                            Long systemAdminId, Integer createdBy) { // CHANGED systemAdminId to Long
        Map<String, Object> response = new HashMap<>();
        
        if (adminUserRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("message", "Username already exists");
            return response;
        }
        
        AdminUser owner = new AdminUser();
        owner.setUsername(username);
        owner.setPassword(passwordEncoder.encode(password)); 
        owner.setRole("location_owner");
        
        owner.setLocationId(locationId);
        owner.setIsActive(true);
        owner.setCreatedBy(createdBy);
        
        AdminUser savedOwner = adminUserRepository.save(owner);
        
        // Fix line 159: systemAdminId is already Long, savedOwner.getAdminId() is Long
        SystemAdminOwner assignment = new SystemAdminOwner(systemAdminId, savedOwner.getAdminId());
        systemAdminOwnerRepository.save(assignment);
        
        response.put("success", true);
        response.put("message", "Owner created and assigned successfully");
        response.put("admin_id", savedOwner.getAdminId());
        response.put("username", savedOwner.getUsername());
        response.put("location_id", savedOwner.getLocationId());
        
        return response;
    }
    
    // Method implementation for deletion (Changed method signature to Long)
    @Transactional
    public Map<String, Object> deleteSystemAdmin(Long systemAdminId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<AdminUser> adminOpt = adminUserRepository.findById(systemAdminId);
        if (adminOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "System admin not found");
            return response;
        }
        
        // Fix line 183: systemAdminId is now Long
        List<SystemAdminOwner> assignments = systemAdminOwnerRepository.findBySystemAdminId(systemAdminId);
        if (!assignments.isEmpty()) {
            response.put("success", false);
            response.put("message", "Cannot delete system admin with assigned owners");
            return response;
        }
        
        adminUserRepository.deleteById(systemAdminId);
        
        response.put("success", true);
        response.put("message", "System admin deleted successfully");
        
        return response;
    }
    
    // Method implementation for deletion (Changed method signature to Long)
    @Transactional
    public Map<String, Object> deleteOwner(Long ownerId) {
        Map<String, Object> response = new HashMap<>();
        
        Optional<AdminUser> ownerOpt = adminUserRepository.findById(ownerId);
        if (ownerOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Owner not found");
            return response;
        }
        
        // Fix line 211: ownerId is now Long
        systemAdminOwnerRepository.deleteByOwnerId(ownerId);
        
        adminUserRepository.deleteById(ownerId);
        
        response.put("success", true);
        response.put("message", "Owner deleted successfully");
        
        return response;
    }
    
    // Method implementation for complete hierarchy (Changed method signature to Long)
    public Map<String, Object> getCompleteHierarchy() {
        Map<String, Object> hierarchy = new HashMap<>();
        
        List<Map<String, Object>> systemAdmins = getAllSystemAdmins();
        
        for (Map<String, Object> systemAdmin : systemAdmins) {
            // admin_id is already a Long, retrieve and cast for consistency if needed, but here it remains Long
            Long systemAdminId = (Long) systemAdmin.get("admin_id");
            List<Map<String, Object>> owners = getOwnersUnderSystemAdmin(systemAdminId);
            
            for (Map<String, Object> owner : owners) {
                Long ownerId = (Long) owner.get("admin_id");
                List<Map<String, Object>> devices = getDevicesUnderOwner(ownerId);
                owner.put("devices", devices);
            }
            
            systemAdmin.put("owners", owners);
        }
        
        hierarchy.put("system_admins", systemAdmins);
        
        return hierarchy;
    }
}
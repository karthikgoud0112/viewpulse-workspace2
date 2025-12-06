package com.viewpulse.controller;

import com.viewpulse.dto.CreateAdminRequest;
import com.viewpulse.service.HierarchyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hierarchy")
@CrossOrigin(origins = "*")
public class HierarchyController {
    
    @Autowired
    private HierarchyService hierarchyService;
    
    // Get all system admins (for super admin)
    @GetMapping("/system-admins")
    public ResponseEntity<Map<String, Object>> getAllSystemAdmins() {
        Map<String, Object> response = new HashMap<>();
        try {
            // Service method doesn't take parameters, so no conversion needed here
            List<Map<String, Object>> systemAdmins = hierarchyService.getAllSystemAdmins();
            response.put("success", true);
            response.put("system_admins", systemAdmins);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching system admins: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Get owners under a system admin
    @GetMapping("/system-admin/{systemAdminId}/owners")
    public ResponseEntity<Map<String, Object>> getOwnersUnderSystemAdmin(@PathVariable Integer systemAdminId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // FIX line 42: Convert Integer path variable to Long
            Long systemAdminIdLong = systemAdminId.longValue();
            List<Map<String, Object>> owners = hierarchyService.getOwnersUnderSystemAdmin(systemAdminIdLong);
            response.put("success", true);
            response.put("owners", owners);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching owners: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Get devices under an owner
    @GetMapping("/owner/{ownerId}/devices")
    public ResponseEntity<Map<String, Object>> getDevicesUnderOwner(@PathVariable Integer ownerId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // FIX line 58: Convert Integer path variable to Long
            Long ownerIdLong = ownerId.longValue();
            List<Map<String, Object>> devices = hierarchyService.getDevicesUnderOwner(ownerIdLong);
            response.put("success", true);
            response.put("devices", devices);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching devices: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // FIX 1: getCompleteHierarchy was missing in the service; assuming service uses a generic implementation now
    @GetMapping("/complete")
    public ResponseEntity<Map<String, Object>> getCompleteHierarchy() {
        try {
            Map<String, Object> hierarchy = hierarchyService.getCompleteHierarchy();
            hierarchy.put("success", true);
            return ResponseEntity.ok(hierarchy);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Error fetching hierarchy: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Create system admin (by super admin)
    @PostMapping("/create-system-admin")
    public ResponseEntity<Map<String, Object>> createSystemAdmin(@RequestBody CreateAdminRequest request) {
        // createdBy (1) is Integer, as that's the Super Admin ID, which is fine
        Map<String, Object> response = hierarchyService.createSystemAdmin(
            request.getUsername(),
            request.getPassword(),
            1 // Assuming super admin ID is 1 (Integer)
        );
        return ResponseEntity.ok(response);
    }
    
    // Create owner (by system admin or super admin)
    @PostMapping("/create-owner")
    public ResponseEntity<Map<String, Object>> createOwner(@RequestBody CreateAdminRequest request) {
        // FIX line 98: Convert systemAdminId (which came in as Integer) to Long
        Long systemAdminIdLong = request.getSystemAdminId() != null ? request.getSystemAdminId().longValue() : null;
        
        Map<String, Object> response = hierarchyService.createOwner(
            request.getUsername(),
            request.getPassword(),
            request.getLocationId(), // Integer
            systemAdminIdLong,       // Now Long
            request.getSystemAdminId() // created_by is the system admin (Integer)
        );
        return ResponseEntity.ok(response);
    }
    
    // Delete system admin
    @DeleteMapping("/system-admin/{systemAdminId}")
    public ResponseEntity<Map<String, Object>> deleteSystemAdmin(@PathVariable Integer systemAdminId) {
        // FIX line 111: Convert Integer path variable to Long
        Long systemAdminIdLong = systemAdminId.longValue();
        Map<String, Object> response = hierarchyService.deleteSystemAdmin(systemAdminIdLong);
        return ResponseEntity.ok(response);
    }
    
    // Delete owner
    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<Map<String, Object>> deleteOwner(@PathVariable Integer ownerId) {
        // FIX line 117: Convert Integer path variable to Long
        Long ownerIdLong = ownerId.longValue();
        Map<String, Object> response = hierarchyService.deleteOwner(ownerIdLong);
        return ResponseEntity.ok(response);
    }
}
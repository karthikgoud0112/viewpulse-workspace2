package com.viewpulse.controller;

import com.viewpulse.model.Video;
import com.viewpulse.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // List all videos
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAllVideos() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Video> videos = videoRepository.findAll();
            response.put("success", true);
            response.put("videos", videos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching videos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Display Screen: Get ACTIVE videos for a specific location
    @GetMapping("/location/{locationId}")
    public ResponseEntity<Map<String, Object>> getVideosByLocation(@PathVariable Integer locationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Video> videos = videoRepository.findByLocationIdAndIsActiveTrue(locationId);
            response.put("success", true);
            response.put("videos", videos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching videos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Admin Panel: Get ALL videos (including inactive) for management
    @GetMapping("/admin/location/{locationId}")
    public ResponseEntity<Map<String, Object>> getVideosByLocationForAdmin(@PathVariable Integer locationId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Video> videos = videoRepository.findByLocationId(locationId);
            response.put("success", true);
            response.put("videos", videos);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching videos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 1. URL Upload (JSON Data)
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> addVideoUrl(@RequestBody Video video) {
        Map<String, Object> response = new HashMap<>();
        try {
            video.setCreatedAt(java.time.LocalDateTime.now());
            if(video.getIsActive() == null) video.setIsActive(true);
            
            videoRepository.save(video);

            response.put("success", true);
            response.put("message", "Video link added successfully");
            response.put("video", video);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error adding video: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // 2. File Upload (Multipart) - FIX: Added Windows/Eclipse compatibility
    @PostMapping("/upload-file")
    public ResponseEntity<Map<String, Object>> uploadVideoFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("videoTitle") String videoTitle,
            @RequestParam("locationId") Integer locationId, 
            @RequestParam(value = "duration", defaultValue = "15") Integer duration
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            // FIX: Use absolute path for Windows compatibility and check permissions
            File baseDir = new File(uploadDir).getAbsoluteFile(); 
            
            // Create directory if not exists, using the absolute path
            if (!baseDir.exists() && !baseDir.mkdirs()) {
                // Throw an explicit error if the directory cannot be created
                throw new java.io.IOException("Failed to create directory: " + baseDir.getAbsolutePath() + ". Check file system permissions.");
            }

            String originalFilename = file.getOriginalFilename();
            String newFileName = System.currentTimeMillis() + "_" + originalFilename;
            
            // Create the target file directly under the absolute directory
            File target = new File(baseDir, newFileName);

            // Transfer the file
            file.transferTo(target);

            // Save video metadata
            Video video = new Video();
            video.setVideoTitle(videoTitle);
            video.setVideoPath(newFileName); // We save just the filename
            video.setLocationId(locationId); 
            video.setDuration(duration);
            video.setCreatedAt(java.time.LocalDateTime.now());
            video.setIsActive(true);
            video.setSequenceOrder(1);
            
            videoRepository.save(video);

            response.put("success", true);
            response.put("message", "Video uploaded successfully");
            response.put("video", video);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Error uploading video: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // Delete a video
    @DeleteMapping("/{videoId}")
    public ResponseEntity<Map<String, Object>> deleteVideo(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (!videoRepository.existsById(videoId)) {
                response.put("success", false);
                response.put("message", "Video not found");
                return ResponseEntity.status(404).body(response);
            }
            videoRepository.deleteById(videoId);
            response.put("success", true);
            response.put("message", "Video deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error deleting video: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    // Toggle video status (Active/Inactive)
    @PutMapping("/{videoId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleVideo(@PathVariable Long videoId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Video video = videoRepository.findById(videoId).orElse(null);
            if (video == null) {
                response.put("success", false);
                response.put("message", "Video not found");
                return ResponseEntity.status(404).body(response);
            }
            
            // Flip the status
            video.setIsActive(video.getIsActive() == null || !video.getIsActive());
            videoRepository.save(video);
            
            response.put("success", true);
            response.put("message", "Video status updated");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error updating video: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
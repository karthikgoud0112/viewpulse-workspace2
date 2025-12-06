package com.viewpulse.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Small compatibility controller to accept older client requests:
 * - POST /api/videos/upload  (multipart/form-data) with file + name OR videoTitle
 * Forwards to existing VideoController.uploadVideoFile(...)
 */
@RestController
@RequestMapping("/api/videos")
public class VideoCompatController {

    @Autowired
    private com.viewpulse.controller.VideoController videoController;

    @PostMapping("/upload")
    public ResponseEntity<?> legacyUpload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "videoTitle", required = false) String videoTitle,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "locationId", required = false) Integer locationId,
            @RequestParam(value = "createdBy", required = false) Integer createdBy
    ) {
        String title = (videoTitle != null && !videoTitle.isEmpty()) ? videoTitle : name;
        // delegate to existing controller method (signature: uploadVideoFile(MultipartFile, String, Integer, Integer))
        return videoController.uploadVideoFile(file, title, locationId, createdBy);
    }
}

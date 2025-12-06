package com.viewpulse.controller;

import com.viewpulse.dto.AdScheduleRequest;
import com.viewpulse.service.AdScheduleService;
import com.viewpulse.repository.VideoRepository;
import com.viewpulse.model.Video;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;

@RestController
@RequestMapping("/api/ad-schedule")
@CrossOrigin(origins = "*")
public class AdScheduleController {

    @Autowired
    private AdScheduleService adScheduleService;

    @Autowired
    private VideoRepository videoRepository;

    // ================================
    // CREATE SCHEDULE
    // ================================
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createSchedule(@RequestBody AdScheduleRequest request) {
        Map<String, Object> response = adScheduleService.createSchedule(
                request.getVideoId(),
                request.getDeviceId(),
                request.getDayOfWeek(),
                request.getStartTime(),
                request.getEndTime(),
                request.getPriority()
        );
        return ResponseEntity.ok(response);
    }

    // ================================
    // GET SCHEDULES FOR DEVICE (admin + display)
    // Returns video_active also
    // ================================
    @GetMapping("/device/{deviceId}")
    public ResponseEntity<Map<String, Object>> getSchedulesForDevice(@PathVariable Long deviceId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Map<String, Object>> schedules = adScheduleService.getSchedulesForDevice(deviceId);

            // Attach video_active flag for each schedule
            for (Map<String, Object> s : schedules) {

                Object videoIdObj = s.get("video_id");
                Boolean videoActive = null;

                if (videoIdObj != null) {
                    try {
                        Long vid = Long.parseLong(videoIdObj.toString());
                        Optional<Video> v = videoRepository.findById(vid);
                        if (v.isPresent()) {
                            videoActive = v.get().getIsActive();
                        }
                    } catch (Exception e) {
                        videoActive = null;
                    }
                }

                // Add flag to JSON response
                s.put("video_active", videoActive);
            }

            response.put("success", true);
            response.put("schedules", schedules);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching schedules: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================================
    // GET ONLY ACTIVE VIDEOS (runtime playback)
    // ================================
    @GetMapping("/device/{deviceId}/active")
    public ResponseEntity<Map<String, Object>> getActiveVideos(@PathVariable Long deviceId) {

        Map<String, Object> response = new HashMap<>();

        try {
            String day = DayOfWeek.from(java.time.LocalDate.now()).name().toLowerCase();
            LocalTime now = LocalTime.now();

            List<Map<String, Object>> videos = adScheduleService.getActiveVideosForDevice(deviceId, day, now);

            // Attach video_active flag
            for (Map<String, Object> vmap : videos) {

                if (!vmap.containsKey("video_active")) {

                    Object vidObj = vmap.get("video_id");
                    Boolean videoActive = null;

                    if (vidObj != null) {
                        try {
                            Long vid = Long.parseLong(vidObj.toString());
                            Optional<Video> v = videoRepository.findById(vid);
                            if (v.isPresent()) {
                                videoActive = v.get().getIsActive();
                            }
                        } catch (Exception ignored) {}
                    }

                    vmap.put("video_active", videoActive);
                }
            }

            response.put("success", true);
            response.put("videos", videos);
            response.put("current_time", now.toString());
            response.put("current_day", day);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error fetching active videos: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    // ================================
    // UPDATE SCHEDULE
    // ================================
    @PutMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody Map<String, Object> updates) {

        Map<String, Object> response = adScheduleService.updateSchedule(
                scheduleId,
                (String) updates.get("day_of_week"),
                (String) updates.get("start_time"),
                (String) updates.get("end_time"),
                (Integer) updates.get("priority"),
                (Boolean) updates.get("is_active")
        );

        return ResponseEntity.ok(response);
    }

    // ================================
    // DELETE SCHEDULE
    // ================================
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<Map<String, Object>> deleteSchedule(@PathVariable Long scheduleId) {
        Map<String, Object> response = adScheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(response);
    }
}

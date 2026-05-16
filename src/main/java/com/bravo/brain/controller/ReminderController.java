package com.bravo.brain.controller;

import com.bravo.brain.model.dto.ReminderDto.*;
import com.bravo.brain.service.NotificationService;
import com.bravo.brain.service.ReminderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/reminders")
@RequiredArgsConstructor
public class ReminderController {

    private final ReminderService reminderService;
    private final NotificationService notificationService;

    // GET /api/reminders/active?store=X&department=Y
    // Şöbə rəhbəri aktiv reminderləri görür
    @GetMapping("/active")
    public ResponseEntity<List<ReminderResponse>> getActiveReminders(
            @RequestParam String store,
            @RequestParam(required = false) String department) {
        return ResponseEntity.ok(reminderService.getActiveReminders(store, department));
    }

    // PUT /api/reminders/{batchId}/resolve
    // Şöbə rəhbəri məhsulu rəfdən götürdü
    @PutMapping("/{batchId}/resolve")
    public ResponseEntity<ResolveResponse> resolveBatch(
            @PathVariable Long batchId,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(reminderService.resolveBatch(batchId, userId));
    }

    // POST /api/reminders/fcm-token
    // Android app FCM token qeyd edir
    @PostMapping("/fcm-token")
    public ResponseEntity<String> registerFcmToken(
            @AuthenticationPrincipal String userId,
            @RequestParam String token) {
        notificationService.registerToken(userId, token);
        return ResponseEntity.ok("FCM token qeyd edildi");
    }

    // POST /api/reminders/test-run (yalnız dev/demo üçün)
    // Reminder scheduler-i manual işlət
    @PostMapping("/test-run")
    public ResponseEntity<String> testRun() {
        reminderService.processReminders2Day();
        reminderService.processReminders1Day();
        return ResponseEntity.ok("Reminder test uğurla tamamlandı");
    }
}
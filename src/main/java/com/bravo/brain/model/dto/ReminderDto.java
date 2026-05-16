package com.bravo.brain.model.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReminderDto {

    // ── AKTİV RİMINDERLƏR CAVABI ──────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class ReminderResponse {
        private Long batchId;
        private String batchCode;
        private String productName;
        private String departmentName;
        private String storeName;
        private Double quantity;
        private LocalDate deliveryDate;
        private LocalDate removalDate;
        private int daysLeft;           // neçə gün qalıb
        private String urgency;         // "CRITICAL" (1 gün) / "WARNING" (2 gün)
        private boolean notified2Day;
        private boolean notified1Day;
    }

    // ── HƏLL EDİLDİ ───────────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class ResolveResponse {
        private String batchCode;
        private String productName;
        private String message;
        private LocalDateTime resolvedAt;
    }

    // ── NOTIFICATION PAYLOAD (Firebase FCM) ───────────────
    @Getter @Setter @AllArgsConstructor
    public static class NotificationPayload {
        private String title;
        private String body;
        private String batchCode;
        private String productName;
        private int daysLeft;
    }
}
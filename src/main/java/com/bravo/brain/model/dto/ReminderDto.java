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
        private Double quantity;          // "2 units remaining" üçün
        private String unit;              // "ədəd" / "kq"
        private LocalDate deliveryDate;
        private LocalDate removalDate;
        private int daysLeft;
        private String urgency;           // "CRITICAL" / "WARNING" / "LOW_STOCK"
        private String urgencyLabel;      // "Expiring" / "Low Stock" / "Action Req"
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
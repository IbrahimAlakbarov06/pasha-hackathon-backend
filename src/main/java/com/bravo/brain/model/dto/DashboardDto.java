package com.bravo.brain.model.dto;

import lombok.*;
import java.util.List;

public class DashboardDto {

    @Getter @Setter @AllArgsConstructor
    public static class DashboardResponse {

        // ── SEKTOR MƏLUMATI ────────────────────────────────
        private String sectorName;          // "Drinks" — departmentName-dən gəlir
        private String storeName;           // "Bravo Koroğlu"

        // ── WASTE KPI ─────────────────────────────────────
        private Double wasteAzn;            // bu ay waste AZN
        private Double wasteChangePercent;  // keçən aya görə dəyişim %
        private String wasteDirection;      // "UP" / "DOWN"

        // ── STOCK HEALTH ──────────────────────────────────
        private Double stockHealthPercent;  // 0-100
        private String stockHealthLabel;    // "Good" / "Warning" / "Critical"

        // ── RİSKLİ BATCHLƏR ──────────────────────────────
        private List<RiskyBatch> riskyBatches;
    }

    @Getter @Setter @AllArgsConstructor
    public static class RiskyBatch {
        private Long batchId;
        private String batchCode;
        private String productName;
        private String productImageBase64;  // məhsulun şəkli
        private Double quantity;
        private String unit;
        private Long hoursLeft;             // neçə saat qalıb
        private Integer daysLeft;           // neçə gün qalıb
        private String urgency;             // "EXPIRING" / "LOW_STOCK" / "QUALITY_ALERT"
        private String urgencyLabel;        // "Expiring Soon" / "Low Stock" / "Quality Alert"
        private String action;              // "LOG_WASTE" / "ORDER_MORE" / "MARK_RESOLVED"
    }
}
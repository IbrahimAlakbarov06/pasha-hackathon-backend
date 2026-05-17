package com.bravo.brain.model.dto;

import lombok.*;
import java.util.List;

public class DirectorDashboardDto {

    @Getter @Setter @AllArgsConstructor
    public static class DirectorDashboardResponse {
        private Double totalWasteAzn;
        private Double wasteChangePercent;
        private String wasteDirection;
        private HighestLossDept highestLossDept;
        private List<DeptWasteShare> departmentDistribution;
        private List<DeptHealth> departmentHealth;
    }

    @Getter @Setter @AllArgsConstructor
    public static class HighestLossDept {
        private String departmentName;
        private Double wasteAzn;
        private String riskLevel;
    }

    @Getter @Setter @AllArgsConstructor
    public static class DeptWasteShare {
        private String departmentName;
        private Double wasteAzn;
        private Double percentage;
    }

    @Getter @Setter @AllArgsConstructor
    public static class DeptHealth {
        private String departmentName;
        private String storeName;
        private String status;       // "CRITICAL" / "WARNING" / "OK"
        private String statusLabel;
        private String issue;
    }
}
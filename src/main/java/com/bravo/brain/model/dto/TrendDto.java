package com.bravo.brain.model.dto;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

public class TrendDto {

    @Getter @Setter @AllArgsConstructor
    public static class TrendResponse {
        private Double totalWasteAzn;           // bu dövrün ümumi waste
        private Double wasteChangePercent;       // keçən aya görə %
        private String wasteDirection;           // "UP" / "DOWN"
        private List<WeeklyWaste> weeklyTrend;  // W1, W2, W3, W4
        private WastePeak wastePeak;             // ən çox waste olan gün
        private List<DeptTrend> departmentTrends; // şöbə üzrə trend
    }

    @Getter @Setter @AllArgsConstructor
    public static class WeeklyWaste {
        private String week;        // "W1", "W2", "W3", "W4"
        private Double wasteAzn;
        private LocalDate from;
        private LocalDate to;
    }

    @Getter @Setter @AllArgsConstructor
    public static class WastePeak {
        private LocalDate date;
        private Double wasteAzn;
    }

    @Getter @Setter @AllArgsConstructor
    public static class DeptTrend {
        private String departmentName;
        private Double thisMonthWaste;
        private Double lastMonthWaste;
        private Double changePercent;   // mənfi = yaxşılaşma, müsbət = artım
        private String direction;       // "UP" / "DOWN"
        private String label;           // "High Turnover" / "Slow Movement" / "Seasonal Shift"
    }
}
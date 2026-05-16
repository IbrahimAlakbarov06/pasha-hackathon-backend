package com.bravo.brain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import java.util.List;

public class AiDto {

    // Java → Python-a göndərilən payload
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PythonRequest {
        @JsonProperty("store_id")
        private String storeId;
        private String date;
        private List<ProductPayload> products;
        @JsonProperty("external_factors")
        private ExternalFactors externalFactors;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductPayload {
        private String name;
        private String category;
        private Double stock;
        @JsonProperty("expiry_days")
        private Integer expiryDays;
        @JsonProperty("daily_sales")
        private Double dailySales;
        @JsonProperty("cost_price")
        private Double costPrice;
        @JsonProperty("selling_price")
        private Double sellingPrice;
        @JsonProperty("temperature_risk")
        private boolean temperatureRisk;
        @JsonProperty("logistics_delay")
        private boolean logisticsDelay;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ExternalFactors {
        private String weather;
        private boolean holiday;
        @JsonProperty("footfall_index")
        private Double footfallIndex;
    }

    // Python-dan gələn cavab
    @Getter @Setter
    public static class PythonResponse {
        private Summary summary;
        @JsonProperty("critical_products")
        private List<CriticalProduct> criticalProducts;
        private List<Recommendation> recommendations;
        @JsonProperty("department_projection")
        private List<DepartmentProjection> departmentProjection;
    }

    @Getter @Setter
    public static class Summary {
        @JsonProperty("total_predicted_waste_azn")
        private Double totalPredictedWasteAzn;
        @JsonProperty("risk_level")
        private String riskLevel;
        @JsonProperty("key_issue")
        private String keyIssue;
    }

    @Getter @Setter
    public static class CriticalProduct {
        private String name;
        @JsonProperty("waste_azn")
        private Double wasteAzn;
        @JsonProperty("risk_score")
        private Integer riskScore;
        private String reason;
    }

    @Getter @Setter
    public static class Recommendation {
        private String type;
        private String product;
        private String action;
        @JsonProperty("expected_impact")
        private String expectedImpact;
    }

    @Getter @Setter
    public static class DepartmentProjection {
        private String category;
        @JsonProperty("waste_azn")
        private Double wasteAzn;
        @JsonProperty("risk_level")
        private String riskLevel;
    }

    // Android/Web-dən gələn request
    @Getter @Setter
    public static class AnalyzeRequest {
        private String store;
        private Long departmentId;
    }
}
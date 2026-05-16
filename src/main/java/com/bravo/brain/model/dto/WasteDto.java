package com.bravo.brain.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;

public class WasteDto {

    @Getter @Setter
    public static class LogRequest {
        @NotNull(message = "Batch ID boş ola bilməz")
        private Long batchId;

        @NotNull @Positive(message = "Miqdar müsbət olmalıdır")
        private Double quantity;

        private String unit;        // "UNITS" / "KG" / "LITERS"

        @NotBlank(message = "Səbəb boş ola bilməz")
        private String reason;      // "EXPIRED" / "DAMAGED" / "QUALITY" / "RECALL" / "OTHER"

        private String notes;

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime wasteDate;
    }

    @Getter @Setter @AllArgsConstructor
    public static class LogResponse {
        private Long wasteLogId;
        private String productName;
        private String batchCode;
        private Double quantity;
        private String unit;
        private String reason;
        private Double estimatedLossAzn;  // qty * costPrice
        private Double unitPrice;
        private LocalDateTime wasteDate;
        private String message;
    }

    // Estimated loss hesablamaq üçün (real-time, confirm etmədən)
    @Getter @Setter
    public static class EstimateRequest {
        @NotNull
        private Long batchId;
        @NotNull @Positive
        private Double quantity;
    }

    @Getter @Setter @AllArgsConstructor
    public static class EstimateResponse {
        private Double estimatedLossAzn;
        private Double unitPrice;
        private Double availableStock;
    }
}
package com.bravo.brain.model.dto;

import com.bravo.brain.model.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductDto {

    // ── YENİ MƏHSUL ────────────────────────────────────────
    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Məhsul adı boş ola bilməz")
        private String name;

        private String barcode;         // ola bilər null

        @NotNull(message = "Kateqoriya seçilməlidir")
        private ProductCategory category;

        @NotNull(message = "Şöbə seçilməlidir")
        private Long departmentId;

        private Double minimumStock;  // isteğe bağlı, default null

        private String unit;            // "ədəd", "kq", "litr"
        private Double costPrice;
        private Double sellPrice;
    }

    // ── YENİ BATCH (mal qəbulu) ────────────────────────────
    @Getter @Setter
    public static class AddBatchRequest {
        @NotNull(message = "Məhsul ID boş ola bilməz")
        private Long productId;

        @NotNull(message = "Miqdar boş ola bilməz")
        @Positive(message = "Miqdar müsbət olmalıdır")
        private Double quantity;

        @NotNull(message = "Gəliş tarixi boş ola bilməz")
        private LocalDate deliveryDate;

        @NotNull(message = "Çıxarılma tarixi boş ola bilməz")
        private LocalDate removalDate;  // şöbə rəhbəri özü təyin edir

        private String addedByUserId;
    }

    // ── SATŞ ────────────────────────────────────────────────
    @Getter @Setter
    public static class SaleRequest {
        @NotBlank(message = "Barkod boş ola bilməz")
        private String barcode;

        @NotNull @Positive
        private Double quantity;
    }

    // ── QAYITMA ────────────────────────────────────────────
    @Getter @Setter
    public static class ReturnRequest {
        @NotNull(message = "Satış ID boş ola bilməz")
        private Long saleId;
    }

    // ── STOK CAVABI ────────────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class StockResponse {
        private Long productId;
        private String productName;
        private String barcode;
        private String category;
        private String departmentName;
        private Double totalStock;      // ümumi stok
        private Integer batchCount;     // neçə batch var
        private LocalDate nearestRemovalDate; // ən yaxın çıxarılma tarixi
    }

    // ── MƏHSUL CAVABI ──────────────────────────────────────
    @Getter @Setter @AllArgsConstructor
    public static class ProductResponse {
        private Long id;
        private String name;
        private String barcode;
        private String category;
        private Double minimumStock;  // isteğe bağlı, default null
        private Long departmentId;
        private String departmentName;  // department.getName()-dən doldurulur
        private String storeName;       // department.getStoreName()-dən doldurulur
        private String imageBase64;
        private String unit;
        private Double costPrice;
        private Double sellPrice;
        private boolean active;
        private LocalDateTime createdAt;
    }
}
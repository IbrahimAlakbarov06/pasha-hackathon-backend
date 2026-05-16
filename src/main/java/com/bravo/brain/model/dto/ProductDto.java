package com.bravo.brain.model.dto;

import com.bravo.brain.model.enums.ProductCategory;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductDto {

    @Getter @Setter
    public static class CreateRequest {
        @NotBlank(message = "Məhsul adı boş ola bilməz")
        private String name;

        private String barcode;

        @NotNull(message = "Kateqoriya seçilməlidir")
        private ProductCategory category;

        @NotNull(message = "Şöbə seçilməlidir")
        private Long departmentId;

        private String imageBase64;
        private Double minimumStock;
        private String unit;
        private Double costPrice;
        private Double sellPrice;
    }

    @Getter @Setter
    public static class AddBatchRequest {
        @NotNull(message = "Məhsul ID boş ola bilməz")
        private Long productId;

        @NotNull @Positive(message = "Miqdar müsbət olmalıdır")
        private Double quantity;

        @NotNull(message = "Gəliş tarixi boş ola bilməz")
        private LocalDate deliveryDate;

        @NotNull(message = "Çıxarılma tarixi boş ola bilməz")
        private LocalDate removalDate;

        private String addedByUserId;
    }

    @Getter @Setter
    public static class SaleRequest {
        @NotBlank(message = "Barkod boş ola bilməz")
        private String barcode;

        @NotNull @Positive
        private Double quantity;
    }

    @Getter @Setter
    public static class ReturnRequest {
        @NotNull(message = "Satış ID boş ola bilməz")
        private Long saleId;
    }

    @Getter @Setter @AllArgsConstructor
    public static class StockResponse {
        private Long productId;
        private String productName;
        private String barcode;
        private String category;
        private Long departmentId;
        private String departmentName;
        private String storeName;
        private Double totalStock;
        private Integer batchCount;
        private LocalDate nearestRemovalDate;
    }

    @Getter @Setter @AllArgsConstructor
    public static class ProductResponse {
        private Long id;
        private String name;
        private String barcode;
        private String category;
        private Long departmentId;
        private String departmentName;
        private String storeName;
        private String imageBase64;
        private Double minimumStock;
        private String unit;
        private Double costPrice;
        private Double sellPrice;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Getter @Setter @AllArgsConstructor
    public static class BarcodeLabelResponse {
        private Long productId;
        private String productName;
        private String barcode;
        private String barcodeImageBase64;
        private String category;
        private String departmentName;
        private String storeName;
        private Double sellPrice;
    }
}
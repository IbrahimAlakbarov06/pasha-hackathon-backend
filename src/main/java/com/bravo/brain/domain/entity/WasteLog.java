package com.bravo.brain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "waste_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WasteLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double costPrice;       // alış qiyməti üzrə ziyan

    @Column(nullable = false)
    private Double totalLoss;       // quantity * costPrice

    @Enumerated(EnumType.STRING)
    private WasteReason reason;

    private String storeName;
    private String departmentName;
    private String resolvedByUserId;

    @Column(nullable = false)
    private LocalDateTime wasteDate;

    @PrePersist
    void prePersist() {
        this.wasteDate = LocalDateTime.now();
        this.totalLoss = this.quantity * this.costPrice;
    }

    public enum WasteReason {
        EXPIRED,   // bitmə tarixi keçdi
        DAMAGED,   // əzildi, xarab oldu
        REMOVED    // şöbə rəhbəri rəfdən götürdü
    }
}
package com.bravo.brain.domain.entity;

import com.bravo.brain.model.enums.BatchStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_batches")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String batchCode;        // sistem tərəfindən yaradılır — FG-0515-001

    @Column(nullable = false)
    private Double quantity;         // cari miqdar

    @Column(nullable = false)
    private Double originalQuantity; // ilkin miqdar

    @Column(nullable = false)
    private LocalDate deliveryDate;  // gəliş tarixi

    @Column(nullable = false)
    private LocalDate removalDate;   // şöbə rəhbərinin təyin etdiyi çıxarılma tarixi

    // Notification statusları
    private boolean notified2Day = false;
    private boolean notified1Day = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BatchStatus status;

    private String addedByUserId;    // hansı şöbə rəhbəri əlavə edib

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = BatchStatus.ACTIVE;
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
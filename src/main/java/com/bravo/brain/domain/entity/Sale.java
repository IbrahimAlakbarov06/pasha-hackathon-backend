package com.bravo.brain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id")
    private ProductBatch batch;     // hansı batch-dan satıldı (FIFO)

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double sellPrice;

    @Column(nullable = false)
    private Double totalAmount;     // quantity * sellPrice

    @Column(nullable = false)
    private boolean returned = false;

    private LocalDateTime returnedAt;

    @Column(nullable = false)
    private LocalDateTime saleDate;

    private String storeName;
    private String departmentName;

    @PrePersist
    void prePersist() {
        this.saleDate = LocalDateTime.now();
        this.totalAmount = this.quantity * this.sellPrice;
    }
}
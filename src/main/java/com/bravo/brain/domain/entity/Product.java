package com.bravo.brain.domain.entity;

import com.bravo.brain.model.enums.ProductCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;            // məhsul ad

    @Column(unique = true)
    private String barcode;         // barkod (ola bilər null — çəkili məhsullar)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(columnDefinition = "TEXT")
    private String imageBase64;

    private Double minimumStock;

    private String unit;            // "ədəd", "kq", "litr"

    private Double costPrice;       // alış qiyməti
    private Double sellPrice;       // satış qiyməti

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
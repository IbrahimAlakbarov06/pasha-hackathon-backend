package com.bravo.brain.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "fcm_tokens")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class FcmToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userId;          // FG-DH-001

    @Column(nullable = false)
    private String token;           // Firebase FCM device token

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void update() {
        this.updatedAt = LocalDateTime.now();
    }
}
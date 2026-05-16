package com.bravo.brain.service;

import com.bravo.brain.domain.entity.FcmToken;
import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.repository.FcmTokenRepository;
import com.bravo.brain.model.dto.ReminderDto.NotificationPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final FcmTokenRepository fcmTokenRepo;

    // ── EXPIRY REMINDER ────────────────────────────────────
    public void sendExpiryReminder(ProductBatch batch, int daysLeft) {
        String userId = batch.getAddedByUserId();
        if (userId == null) {
            log.warn("Batch {} üçün userId yoxdur", batch.getBatchCode());
            return;
        }
        fcmTokenRepo.findByUserId(userId).ifPresentOrElse(
                fcmToken -> sendExpiry(fcmToken, batch, daysLeft),
                () -> log.warn("User {} üçün FCM token tapılmadı", userId)
        );
    }

    private void sendExpiry(FcmToken fcmToken, ProductBatch batch, int daysLeft) {
        String title = daysLeft == 1
                ? "⛔ SON XATIRLATMA — " + batch.getProduct().getName()
                : "⚠️ Xatırlatma — " + batch.getProduct().getName();
        String body = daysLeft == 1
                ? batch.getProduct().getName() + " bu gün rəfdən qaldırılmalıdır!"
                : batch.getProduct().getName() + " sabah rəfdən qaldırılmalıdır!";

        log.info("📱 EXPIRY NOTIFICATION → {} | {} | {}", fcmToken.getUserId(), title, body);
        // Firebase: FirebaseMessaging.getInstance().send(...)
    }

    // ── LOW STOCK ALERT ────────────────────────────────────
    public void sendLowStockAlert(Product product, Double currentStock) {
        String departmentName = product.getDepartment().getName();
        String storeName = product.getDepartment().getStoreName();

        log.info("📦 LOW STOCK ALERT — {} | Qalan: {} {} | Şöbə: {} | Mağaza: {}",
                product.getName(), currentStock, product.getUnit(), departmentName, storeName);

        // Həmin şöbəyə aid userləri tap və notification göndər
        // Real implementasiyada UserRepository-dən department head-ləri tapıb FCM göndəririk
        // Mock üçün log kifayətdir
    }

    // ── FCM TOKEN QEYD ET ──────────────────────────────────
    public void registerToken(String userId, String token) {
        FcmToken fcmToken = fcmTokenRepo.findByUserId(userId)
                .orElse(FcmToken.builder().userId(userId).build());
        fcmToken.setToken(token);
        fcmTokenRepo.save(fcmToken);
        log.info("FCM token qeyd edildi: {}", userId);
    }
}
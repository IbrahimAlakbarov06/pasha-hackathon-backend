package com.bravo.brain.service;

import com.bravo.brain.domain.entity.FcmToken;
import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.repository.FcmTokenRepository;
import com.google.firebase.messaging.*;
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
                fcmToken -> sendFcmNotification(
                        fcmToken.getToken(),
                        daysLeft == 1
                                ? "⛔ Son Xatırlatma — " + batch.getProduct().getName()
                                : "⚠️ Xatırlatma — " + batch.getProduct().getName(),
                        daysLeft == 1
                                ? batch.getProduct().getName() + " bu gün rəfdən qaldırılmalıdır!"
                                : batch.getProduct().getName() + " sabah rəfdən qaldırılmalıdır!",
                        batch.getBatchCode()
                ),
                () -> log.warn("User {} üçün FCM token tapılmadı", userId)
        );
    }

    // ── LOW STOCK ALERT ────────────────────────────────────
    public void sendLowStockAlert(Product product, Double currentStock) {
        log.info("📦 LOW STOCK — {} | Qalan: {}", product.getName(), currentStock);
        // Department head-in FCM token-ini tap və göndər
        // UserRepository-dən department-a aid useri tapıb göndərmək olar
    }

    // ── FCM TOKEN QEYD ET ──────────────────────────────────
    public void registerToken(String userId, String token) {
        FcmToken fcmToken = fcmTokenRepo.findByUserId(userId)
                .orElse(FcmToken.builder().userId(userId).build());
        fcmToken.setToken(token);
        fcmTokenRepo.save(fcmToken);
        log.info("FCM token qeyd edildi: {}", userId);
    }

    // ── REAL FCM GÖNDƏR ────────────────────────────────────
    private void sendFcmNotification(String token, String title,
                                     String body, String batchCode) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .putData("batchCode", batchCode)
                    .putData("type", "EXPIRY_REMINDER")
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM göndərildi: {} → {}", token, response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM xətası: {}", e.getMessage());

            // Token etibarsızdırsa DB-dən sil
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED ||
                    e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                fcmTokenRepo.findByUserId(token).ifPresent(fcmTokenRepo::delete);
                log.warn("Etibarsız FCM token silindi");
            }
        }
    }
}
package com.bravo.brain.service;

import com.bravo.brain.domain.entity.FcmToken;
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

    // ── PUSH NOTIFICATION GÖNDƏR ───────────────────────────
    public void sendExpiryReminder(ProductBatch batch, int daysLeft) {
        String userId = batch.getAddedByUserId();
        if (userId == null) {
            log.warn("Batch {} üçün userId yoxdur — notification göndərilmir", batch.getBatchCode());
            return;
        }

        fcmTokenRepo.findByUserId(userId).ifPresentOrElse(
                fcmToken -> send(fcmToken, buildPayload(batch, daysLeft)),
                () -> log.warn("User {} üçün FCM token tapılmadı", userId)
        );
    }

    private void send(FcmToken fcmToken, NotificationPayload payload) {
        // Real implementasiyada Firebase Admin SDK istifadə olunur:
        // FirebaseMessaging.getInstance().send(Message.builder()
        //     .setToken(fcmToken.getToken())
        //     .setNotification(Notification.builder()
        //         .setTitle(payload.getTitle())
        //         .setBody(payload.getBody())
        //         .build())
        //     .build());

        // Mock implementasiya — hackathon üçün log ilə simulasiya
        log.info("📱 PUSH NOTIFICATION göndərildi → {}", fcmToken.getUserId());
        log.info("   Başlıq: {}", payload.getTitle());
        log.info("   Mətn:   {}", payload.getBody());
        log.info("   Batch:  {}", payload.getBatchCode());
    }

    private NotificationPayload buildPayload(ProductBatch batch, int daysLeft) {
        String title = daysLeft == 1
                ? "⛔ SON XATIRLATMA — " + batch.getProduct().getName()
                : "⚠️ Xatırlatma — " + batch.getProduct().getName();

        String body = daysLeft == 1
                ? batch.getProduct().getName() + " bu gün rəfdən qaldırılmalıdır!"
                : batch.getProduct().getName() + " sabah rəfdən qaldırılmalıdır!";

        return new NotificationPayload(
                title, body,
                batch.getBatchCode(),
                batch.getProduct().getName(),
                daysLeft
        );
    }

    // ── FCM TOKEN QEYD ET / YENİLƏ ────────────────────────
    public void registerToken(String userId, String token) {
        FcmToken fcmToken = fcmTokenRepo.findByUserId(userId)
                .orElse(FcmToken.builder().userId(userId).build());
        fcmToken.setToken(token);
        fcmTokenRepo.save(fcmToken);
        log.info("FCM token qeyd edildi: {}", userId);
    }
}
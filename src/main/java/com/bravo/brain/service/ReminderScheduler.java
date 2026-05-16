package com.bravo.brain.service;

import com.bravo.brain.service.ReminderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderService reminderService;

    // ── HƏR GÜN SƏHƏR 8:00 ────────────────────────────────
    @Scheduled(cron = "0 0 8 * * *")
    public void runDailyReminders() {
        log.info("⏰ Reminder Scheduler başladı — {}", LocalDateTime.now());

        // 2 gün qalan məhsullar
        reminderService.processReminders2Day();

        // 1 gün qalan məhsullar — son xatırlatma
        reminderService.processReminders1Day();

        log.info("✅ Reminder Scheduler tamamlandı — {}", LocalDateTime.now());
    }

    // ── TEST ÜÇÜN — hər dəqiqə (demo zamanı istifadə et) ──
    // @Scheduled(fixedRate = 60000)
    // public void runTestReminders() {
    //     runDailyReminders();
    // }
}
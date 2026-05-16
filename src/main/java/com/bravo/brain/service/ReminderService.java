package com.bravo.brain.service;

import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.entity.WasteLog;
import com.bravo.brain.domain.repository.ProductBatchRepository;
import com.bravo.brain.domain.repository.WasteLogRepository;
import com.bravo.brain.model.dto.ReminderDto.*;
import com.bravo.brain.model.enums.BatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReminderService {

    private final ProductBatchRepository batchRepo;
    private final WasteLogRepository wasteRepo;
    private final NotificationService notificationService;

    // ── AKTİV RİMINDERLƏR ─────────────────────────────────
    public List<ReminderResponse> getActiveReminders(String storeName, String departmentName) {
        LocalDate warningDate = LocalDate.now().plusDays(2);

        return batchRepo.findAtRisk(warningDate).stream()
                .filter(b -> b.getProduct().getDepartment().getStoreName().equals(storeName))
                .filter(b -> departmentName == null ||
                        b.getProduct().getDepartment().getName().equals(departmentName))
                .map(this::toReminderResponse)
                .collect(Collectors.toList());
    }

    // ── HƏLL EDİLDİ ───────────────────────────────────────
    @Transactional
    public ResolveResponse resolveBatch(Long batchId, String resolvedByUserId) {
        ProductBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch tapılmadı"));

        if (batch.getStatus() != BatchStatus.ACTIVE)
            throw new RuntimeException("Bu batch artıq aktiv deyil");

        if (batch.getQuantity() > 0) {
            WasteLog wasteLog = WasteLog.builder()
                    .product(batch.getProduct())
                    .batch(batch)
                    .quantity(batch.getQuantity())
                    .costPrice(batch.getProduct().getCostPrice() != null
                            ? batch.getProduct().getCostPrice() : 0.0)
                    .reason(WasteLog.WasteReason.REMOVED)
                    .resolvedByUserId(resolvedByUserId)
                    .build();
            wasteRepo.save(wasteLog);
        }

        batch.setStatus(BatchStatus.REMOVED);
        batch.setQuantity(0.0);
        batchRepo.save(batch);

        return new ResolveResponse(
                batch.getBatchCode(),
                batch.getProduct().getName(),
                "Məhsul rəfdən götürüldü və waste kimi qeyd edildi",
                LocalDateTime.now()
        );
    }

    // ── 2 GÜNLÜK REMINDER ─────────────────────────────────
    @Transactional
    public void processReminders2Day() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<ProductBatch> batches = batchRepo
                .findByRemovalDateAndStatusAndNotified2DayFalse(targetDate, BatchStatus.ACTIVE);

        for (ProductBatch batch : batches) {
            notificationService.sendExpiryReminder(batch, 2);
            batch.setNotified2Day(true);
            batchRepo.save(batch);
        }
        log.info("2 günlük reminder: {} məhsul", batches.size());
    }

    // ── 1 GÜNLÜK REMINDER ─────────────────────────────────
    @Transactional
    public void processReminders1Day() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        List<ProductBatch> batches = batchRepo
                .findByRemovalDateAndStatusAndNotified1DayFalse(targetDate, BatchStatus.ACTIVE);

        for (ProductBatch batch : batches) {
            notificationService.sendExpiryReminder(batch, 1);
            batch.setNotified1Day(true);
            batchRepo.save(batch);
        }
        log.info("1 günlük reminder: {} məhsul", batches.size());
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private ReminderResponse toReminderResponse(ProductBatch batch) {
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), batch.getRemovalDate());
        return new ReminderResponse(
                batch.getId(),
                batch.getBatchCode(),
                batch.getProduct().getName(),
                batch.getProduct().getDepartment().getName(),
                batch.getProduct().getDepartment().getStoreName(),
                batch.getQuantity(),
                batch.getDeliveryDate(),
                batch.getRemovalDate(),
                daysLeft,
                daysLeft <= 1 ? "CRITICAL" : "WARNING",
                batch.isNotified2Day(),
                batch.isNotified1Day()
        );
    }
}
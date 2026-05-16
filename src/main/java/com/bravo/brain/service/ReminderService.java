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

    // ── AKTİV RİMINDERLƏR — şöbə rəhbəri görür ───────────
    public List<ReminderResponse> getActiveReminders(String storeName, String departmentName) {
        LocalDate warningDate = LocalDate.now().plusDays(2);

        return batchRepo.findAtRisk(warningDate).stream()
                .filter(b -> b.getProduct().getStoreName().equals(storeName))
                .filter(b -> departmentName == null ||
                        b.getProduct().getDepartmentName().equals(departmentName))
                .map(this::toReminderResponse)
                .collect(Collectors.toList());
    }

    // ── HƏLL EDİLDİ — şöbə rəhbəri rəfdən götürdü ────────
    @Transactional
    public ResolveResponse resolveBatch(Long batchId, String resolvedByUserId) {
        ProductBatch batch = batchRepo.findById(batchId)
                .orElseThrow(() -> new RuntimeException("Batch tapılmadı"));

        if (batch.getStatus() != BatchStatus.ACTIVE)
            throw new RuntimeException("Bu batch artıq aktiv deyil");

        // Qalan miqdarı waste kimi qeyd et
        if (batch.getQuantity() > 0) {
            WasteLog wasteLog = WasteLog.builder()
                    .product(batch.getProduct())
                    .batch(batch)
                    .quantity(batch.getQuantity())
                    .costPrice(batch.getProduct().getCostPrice() != null
                            ? batch.getProduct().getCostPrice() : 0.0)
                    .reason(WasteLog.WasteReason.REMOVED)
                    .storeName(batch.getProduct().getStoreName())
                    .departmentName(batch.getProduct().getDepartmentName())
                    .resolvedByUserId(resolvedByUserId)
                    .build();
            wasteRepo.save(wasteLog);
        }

        batch.setStatus(BatchStatus.REMOVED);
        batch.setQuantity(0.0);
        batchRepo.save(batch);

        log.info("Batch {} rəfdən götürüldü — {}", batch.getBatchCode(), resolvedByUserId);

        return new ResolveResponse(
                batch.getBatchCode(),
                batch.getProduct().getName(),
                "Məhsul rəfdən götürüldü və waste kimi qeyd edildi",
                LocalDateTime.now()
        );
    }

    // ── SCHEDULER TƏRƏFINDƏN ÇAĞIRILIR ────────────────────
    // 2 gün qalınca notification göndər
    @Transactional
    public void processReminders2Day() {
        LocalDate targetDate = LocalDate.now().plusDays(2);
        List<ProductBatch> batches = batchRepo
                .findByRemovalDateAndStatusAndNotified2DayFalse(targetDate, BatchStatus.ACTIVE);

        for (ProductBatch batch : batches) {
            notificationService.sendExpiryReminder(batch, 2);
            batch.setNotified2Day(true);
            batchRepo.save(batch);
            log.info("2 günlük reminder göndərildi: {}", batch.getBatchCode());
        }
        log.info("2 günlük reminder batch: {} məhsul", batches.size());
    }

    // 1 gün qalınca son xatırlatma
    @Transactional
    public void processReminders1Day() {
        LocalDate targetDate = LocalDate.now().plusDays(1);
        List<ProductBatch> batches = batchRepo
                .findByRemovalDateAndStatusAndNotified1DayFalse(targetDate, BatchStatus.ACTIVE);

        for (ProductBatch batch : batches) {
            notificationService.sendExpiryReminder(batch, 1);
            batch.setNotified1Day(true);
            batchRepo.save(batch);
            log.info("1 günlük son reminder göndərildi: {}", batch.getBatchCode());
        }
        log.info("1 günlük reminder batch: {} məhsul", batches.size());
    }

    // ── ENTITY → DTO ──────────────────────────────────────
    private ReminderResponse toReminderResponse(ProductBatch batch) {
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), batch.getRemovalDate());
        String urgency = daysLeft <= 1 ? "CRITICAL" : "WARNING";

        return new ReminderResponse(
                batch.getId(),
                batch.getBatchCode(),
                batch.getProduct().getName(),
                batch.getProduct().getDepartmentName(),
                batch.getProduct().getStoreName(),
                batch.getQuantity(),
                batch.getDeliveryDate(),
                batch.getRemovalDate(),
                daysLeft,
                urgency,
                batch.isNotified2Day(),
                batch.isNotified1Day()
        );
    }
}
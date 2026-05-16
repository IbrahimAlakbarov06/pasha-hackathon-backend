package com.bravo.brain.service;

import com.bravo.brain.domain.entity.*;
import com.bravo.brain.domain.repository.*;
import com.bravo.brain.model.dto.WasteDto;
import com.bravo.brain.model.enums.BatchStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WasteService {

    private final ProductBatchRepository batchRepo;
    private final WasteLogRepository wasteRepo;
    private final ProductRepository productRepo;

    // ── ESTIMATED LOSS — real-time hesablama ──────────────
    public WasteDto.EstimateResponse estimate(WasteDto.EstimateRequest req) {
        ProductBatch batch = batchRepo.findById(req.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch tapılmadı"));

        double costPrice = batch.getProduct().getCostPrice() != null
                ? batch.getProduct().getCostPrice() : 0.0;

        double estimatedLoss = req.getQuantity() * costPrice;

        return new WasteDto.EstimateResponse(
                Math.round(estimatedLoss * 100.0) / 100.0,
                costPrice,
                batch.getQuantity()
        );
    }

    // ── WASTE LOG YAZ ─────────────────────────────────────
    @Transactional
    public WasteDto.LogResponse logWaste(WasteDto.LogRequest req, String resolvedByUserId) {
        ProductBatch batch = batchRepo.findById(req.getBatchId())
                .orElseThrow(() -> new RuntimeException("Batch tapılmadı"));

        if (batch.getStatus() != BatchStatus.ACTIVE)
            throw new RuntimeException("Bu batch artıq aktiv deyil");

        if (req.getQuantity() > batch.getQuantity())
            throw new RuntimeException("Stokda yalnız " + batch.getQuantity() + " var");

        Product product = batch.getProduct();
        double costPrice = product.getCostPrice() != null ? product.getCostPrice() : 0.0;
        double totalLoss = req.getQuantity() * costPrice;

        // Waste reason map
        WasteLog.WasteReason reason = parseReason(req.getReason());

        // Waste log yaz
        WasteLog wasteLog = WasteLog.builder()
                .product(product)
                .batch(batch)
                .quantity(req.getQuantity())
                .costPrice(costPrice)
                .reason(reason)
                .storeName(product.getDepartment().getStoreName())
                .departmentName(product.getDepartment().getName())
                .resolvedByUserId(resolvedByUserId)
                .build();
        wasteRepo.save(wasteLog);

        // Batch stokunu azalt
        batch.setQuantity(batch.getQuantity() - req.getQuantity());
        if (batch.getQuantity() <= 0) {
            batch.setQuantity(0.0);
            batch.setStatus(BatchStatus.WASTED);
        }
        batchRepo.save(batch);

        log.info("Waste log: {} | {} ədəd | {} AZN zərər | {}",
                product.getName(), req.getQuantity(), totalLoss, reason);

        return new WasteDto.LogResponse(
                wasteLog.getId(),
                product.getName(),
                batch.getBatchCode(),
                req.getQuantity(),
                req.getUnit(),
                reason.name(),
                Math.round(totalLoss * 100.0) / 100.0,
                costPrice,
                req.getWasteDate() != null ? req.getWasteDate() : LocalDateTime.now(),
                "Waste uğurla qeyd edildi"
        );
    }

    private WasteLog.WasteReason parseReason(String reason) {
        return switch (reason.toUpperCase()) {
            case "EXPIRED"  -> WasteLog.WasteReason.EXPIRED;
            case "DAMAGED"  -> WasteLog.WasteReason.DAMAGED;
            case "QUALITY"  -> WasteLog.WasteReason.QUALITY;
            case "RECALL"   -> WasteLog.WasteReason.RECALL;
            case "OTHER"    -> WasteLog.WasteReason.OTHER;
            default         -> WasteLog.WasteReason.REMOVED;
        };
    }
}
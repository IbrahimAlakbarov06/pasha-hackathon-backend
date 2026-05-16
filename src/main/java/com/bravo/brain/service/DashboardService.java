package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Department;
import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.repository.*;
import com.bravo.brain.model.dto.DashboardDto.*;
import com.bravo.brain.model.enums.BatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final WasteLogRepository wasteRepo;
    private final ProductBatchRepository batchRepo;
    private final ProductRepository productRepo;
    private final DepartmentRepository departmentRepo;

    public DashboardResponse getDashboard(String storeName, Long departmentId) {

        Department department = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Şöbə tapılmadı"));

        String departmentName = department.getName();

        // ── WASTE KPI ─────────────────────────────────────
        LocalDateTime monthStart = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);

        Double thisMonthWaste = wasteRepo.getTotalWaste(departmentName, storeName, monthStart, now);
        Double lastMonthWaste = wasteRepo.getTotalWaste(departmentName, storeName, lastMonthStart, lastMonthEnd);

        double wasteChange = 0.0;
        String wasteDirection = "DOWN";
        if (lastMonthWaste != null && lastMonthWaste > 0) {
            wasteChange = ((thisMonthWaste - lastMonthWaste) / lastMonthWaste) * 100;
            wasteDirection = wasteChange >= 0 ? "UP" : "DOWN";
            wasteChange = Math.abs(wasteChange);
        }

        // ── STOCK HEALTH ──────────────────────────────────
        List<ProductBatch> allActiveBatches = batchRepo
                .findActiveByDepartmentAndStore(departmentName, storeName);

        double stockHealth = calculateStockHealth(allActiveBatches);
        String stockLabel = stockHealth >= 80 ? "Good" : stockHealth >= 50 ? "Warning" : "Critical";

        // ── RİSKLİ BATCHLƏR ──────────────────────────────
        LocalDate riskDate = LocalDate.now().plusDays(7);
        List<ProductBatch> atRisk = batchRepo.findAtRisk(riskDate).stream()
                .filter(b -> b.getProduct().getDepartment().getName().equals(departmentName))
                .filter(b -> b.getProduct().getDepartment().getStoreName().equals(storeName))
                .collect(Collectors.toList());

        List<RiskyBatch> riskyBatches = atRisk.stream()
                .map(this::toRiskyBatch)
                .collect(Collectors.toList());

        // Low stock məhsullar
        List<Product> products = productRepo
                .findByDepartment_StoreNameAndDepartment_Name(storeName, departmentName);

        for (Product product : products) {
            if (product.getMinimumStock() == null) continue;

            List<ProductBatch> batches = batchRepo
                    .findActiveByProductOrderByDeliveryDate(product.getId());
            double totalStock = batches.stream().mapToDouble(ProductBatch::getQuantity).sum();

            if (totalStock <= product.getMinimumStock()) {
                boolean alreadyAdded = riskyBatches.stream()
                        .anyMatch(rb -> batches.stream()
                                .anyMatch(b -> b.getId().toString().equals(rb.getBatchId().toString())));
                if (!alreadyAdded && !batches.isEmpty()) {
                    ProductBatch firstBatch = batches.get(0);
                    riskyBatches.add(new RiskyBatch(
                            firstBatch.getId(),
                            firstBatch.getBatchCode(),
                            product.getName(),
                            product.getImageBase64(),
                            totalStock,
                            product.getUnit(),
                            null,
                            null,
                            "LOW_STOCK",
                            "Low Stock",
                            "ORDER_MORE"
                    ));
                }
            }
        }

        return new DashboardResponse(
                departmentName,
                storeName,
                thisMonthWaste != null ? thisMonthWaste : 0.0,
                Math.round(wasteChange * 10.0) / 10.0,
                wasteDirection,
                Math.round(stockHealth * 10.0) / 10.0,
                stockLabel,
                riskyBatches
        );
    }

    // ── STOCK HEALTH HESABLA ──────────────────────────────
    // Bitmə tarixinə 3 gündən az qalan batchlər "xəstədir"
    private double calculateStockHealth(List<ProductBatch> batches) {
        if (batches.isEmpty()) return 100.0;

        long healthy = batches.stream()
                .filter(b -> b.getRemovalDate() != null &&
                        ChronoUnit.DAYS.between(LocalDate.now(), b.getRemovalDate()) > 3)
                .count();

        return (healthy * 100.0) / batches.size();
    }

    // ── BATCH → DTO ───────────────────────────────────────
    private RiskyBatch toRiskyBatch(ProductBatch batch) {
        long hoursLeft = ChronoUnit.HOURS.between(
                LocalDateTime.now(),
                batch.getRemovalDate().atTime(23, 59));
        int daysLeft = (int) ChronoUnit.DAYS.between(LocalDate.now(), batch.getRemovalDate());

        String urgency = daysLeft <= 0 ? "QUALITY_ALERT"
                : daysLeft <= 1 ? "EXPIRING"
                  : "EXPIRING";

        String urgencyLabel = daysLeft <= 0 ? "Quality Alert"
                : daysLeft <= 1 ? "Expiring Soon"
                  : "Expiring Soon";

        String action = daysLeft <= 0 ? "LOG_WASTE" : "MARK_RESOLVED";

        return new RiskyBatch(
                batch.getId(),
                batch.getBatchCode(),
                batch.getProduct().getName(),
                batch.getProduct().getImageBase64(),
                batch.getQuantity(),
                batch.getProduct().getUnit(),
                hoursLeft,
                daysLeft,
                urgency,
                urgencyLabel,
                action
        );
    }
}
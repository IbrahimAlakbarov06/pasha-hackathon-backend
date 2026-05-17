package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Department;
import com.bravo.brain.domain.repository.DepartmentRepository;
import com.bravo.brain.domain.repository.ProductBatchRepository;
import com.bravo.brain.domain.repository.WasteLogRepository;
import com.bravo.brain.model.dto.DirectorDashboardDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DirectorDashboardService {

    private final WasteLogRepository wasteRepo;
    private final DepartmentRepository departmentRepo;
    private final ProductBatchRepository batchRepo;

    public DirectorDashboardResponse getDirectorDashboard() {

        LocalDateTime monthStart = LocalDateTime.now()
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);

        // ── BÜTÜN AKTİV ŞÖBƏLƏRİ GƏTİR ──────────────────
        List<Department> departments = departmentRepo.findAll().stream()
                .filter(Department::isActive)
                .collect(Collectors.toList());

        // ── HƏR ŞÖBƏNİN WASTE-İNİ HESABLA ───────────────
        List<DeptWasteShare> deptWastes = new ArrayList<>();
        double totalWaste = 0.0;

        for (Department dept : departments) {
            Double waste = wasteRepo.getTotalWaste(
                    dept.getName(), dept.getStoreName(), monthStart, now);
            if (waste == null) waste = 0.0;
            totalWaste += waste;
            deptWastes.add(new DeptWasteShare(dept.getName(), waste, 0.0));
        }

        // ── FAİZ HESABLA ──────────────────────────────────
        final double finalTotal = totalWaste;
        if (finalTotal > 0) {
            deptWastes = deptWastes.stream()
                    .map(d -> new DeptWasteShare(
                            d.getDepartmentName(),
                            d.getWasteAzn(),
                            Math.round((d.getWasteAzn() / finalTotal * 100) * 10.0) / 10.0
                    ))
                    .sorted(Comparator.comparingDouble(DeptWasteShare::getWasteAzn).reversed())
                    .collect(Collectors.toList());
        }

        // ── ƏN ÇOX ZİYAN VERƏN ŞÖBƏ ──────────────────────
        DeptWasteShare highest = deptWastes.isEmpty() ? null : deptWastes.get(0);
        HighestLossDept highestLossDept = highest != null
                ? new HighestLossDept(
                highest.getDepartmentName(),
                highest.getWasteAzn(),
                highest.getWasteAzn() > 1000 ? "HIGH"
                : highest.getWasteAzn() > 500 ? "MEDIUM" : "LOW")
                : new HighestLossDept("N/A", 0.0, "LOW");

        // ── WASTE DƏYİŞİMİ ────────────────────────────────
        Double lastMonthTotal = 0.0;
        for (Department dept : departments) {
            Double w = wasteRepo.getTotalWaste(
                    dept.getName(), dept.getStoreName(), lastMonthStart, lastMonthEnd);
            if (w != null) lastMonthTotal += w;
        }

        double changePercent = 0.0;
        String direction = "DOWN";
        if (lastMonthTotal > 0) {
            changePercent = ((totalWaste - lastMonthTotal) / lastMonthTotal) * 100;
            direction = changePercent >= 0 ? "UP" : "DOWN";
            changePercent = Math.abs(changePercent);
        }

        // ── ŞÖBƏ SAĞLIĞI ──────────────────────────────────
        List<DeptHealth> healthList = new ArrayList<>();
        for (Department dept : departments) {
            long criticalBatches = batchRepo.findAtRisk(LocalDate.now())
                    .stream()
                    .filter(b -> b.getProduct().getDepartment().getId().equals(dept.getId()))
                    .count();

            long warningBatches = batchRepo.findAtRisk(LocalDate.now().plusDays(2))
                    .stream()
                    .filter(b -> b.getProduct().getDepartment().getId().equals(dept.getId()))
                    .count();

            String status, statusLabel, issue;
            if (criticalBatches > 0) {
                status = "CRITICAL";
                statusLabel = "Critical";
                issue = criticalBatches + " product(s) expired or expiring today";
            } else if (warningBatches > 0) {
                status = "WARNING";
                statusLabel = "Warning";
                issue = warningBatches + " product(s) expiring within 2 days";
            } else {
                status = "OK";
                statusLabel = "Healthy";
                issue = "No immediate risks detected";
            }

            healthList.add(new DeptHealth(
                    dept.getName(),
                    dept.getStoreName(),
                    status,
                    statusLabel,
                    issue
            ));
        }

        return new DirectorDashboardResponse(
                Math.round(totalWaste * 100.0) / 100.0,
                Math.round(changePercent * 10.0) / 10.0,
                direction,
                highestLossDept,
                deptWastes,
                healthList
        );
    }
}
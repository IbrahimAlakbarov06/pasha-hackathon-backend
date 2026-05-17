package com.bravo.brain.service;

import com.bravo.brain.domain.entity.Department;
import com.bravo.brain.domain.entity.WasteLog;
import com.bravo.brain.domain.repository.DepartmentRepository;
import com.bravo.brain.domain.repository.WasteLogRepository;
import com.bravo.brain.model.dto.TrendDto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final WasteLogRepository wasteRepo;
    private final DepartmentRepository departmentRepo;

    public TrendResponse getTrends(String storeName, Long departmentId) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime lastMonthStart = monthStart.minusMonths(1);
        LocalDateTime lastMonthEnd = monthStart.minusSeconds(1);

        // ── ŞÖBƏNİ TAP ───────────────────────────────────
        Department dept = departmentRepo.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Şöbə tapılmadı"));
        String deptName = dept.getName();

        // ── BU AY VƏ KEÇƏN AY ────────────────────────────
        Double thisMonth = wasteRepo.getTotalWaste(deptName, storeName, monthStart, now);
        Double lastMonth = wasteRepo.getTotalWaste(deptName, storeName, lastMonthStart, lastMonthEnd);
        if (thisMonth == null) thisMonth = 0.0;
        if (lastMonth == null) lastMonth = 0.0;

        double changePercent = 0.0;
        String direction = "DOWN";
        if (lastMonth > 0) {
            changePercent = ((thisMonth - lastMonth) / lastMonth) * 100;
            direction = changePercent >= 0 ? "UP" : "DOWN";
            changePercent = Math.abs(changePercent);
        }

        // ── HƏFTƏLİK TREND (W1-W4) ───────────────────────
        List<WeeklyWaste> weeklyTrend = new ArrayList<>();
        LocalDate monthStartDate = LocalDate.now().withDayOfMonth(1);

        for (int i = 0; i < 4; i++) {
            LocalDate weekStart = monthStartDate.plusDays((long) i * 7);
            LocalDate weekEnd = weekStart.plusDays(6);
            if (weekEnd.isAfter(LocalDate.now())) weekEnd = LocalDate.now();

            Double weekWaste = wasteRepo.getTotalWaste(
                    deptName, storeName,
                    weekStart.atStartOfDay(),
                    weekEnd.atTime(23, 59, 59));

            weeklyTrend.add(new WeeklyWaste(
                    "W" + (i + 1),
                    weekWaste != null ? Math.round(weekWaste * 100.0) / 100.0 : 0.0,
                    weekStart,
                    weekEnd
            ));
        }

        // ── WASTE PEAK — ən çox waste olan gün ───────────
        List<WasteLog> allLogs = wasteRepo.findByDepartmentAndDateBetween(
                deptName, monthStart, now);

        WastePeak wastePeak = null;
        if (!allLogs.isEmpty()) {
            Map<LocalDate, Double> byDay = allLogs.stream()
                    .collect(Collectors.groupingBy(
                            w -> w.getWasteDate().toLocalDate(),
                            Collectors.summingDouble(WasteLog::getTotalLoss)
                    ));

            Map.Entry<LocalDate, Double> peak = byDay.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);

            if (peak != null) {
                wastePeak = new WastePeak(peak.getKey(),
                        Math.round(peak.getValue() * 100.0) / 100.0);
            }
        }

        if (wastePeak == null) {
            wastePeak = new WastePeak(LocalDate.now(), 0.0);
        }

        // ── ŞÖBƏ TRENDLƏRİ ───────────────────────────────
        List<Department> allDepts = departmentRepo
                .findByStoreNameAndActiveTrue(storeName);

        List<DeptTrend> deptTrends = allDepts.stream().map(d -> {
            Double thisW = wasteRepo.getTotalWaste(
                    d.getName(), storeName, monthStart, now);
            Double lastW = wasteRepo.getTotalWaste(
                    d.getName(), storeName, lastMonthStart, lastMonthEnd);
            if (thisW == null) thisW = 0.0;
            if (lastW == null) lastW = 0.0;

            double pct = 0.0;
            String dir = "DOWN";
            if (lastW > 0) {
                pct = ((thisW - lastW) / lastW) * 100;
                dir = pct >= 0 ? "UP" : "DOWN";
                pct = Math.abs(pct);
            }

            // Label məntiqi
            String label;
            if (dir.equals("DOWN") && pct > 5) label = "Improvement";
            else if (dir.equals("UP") && pct > 10) label = "Needs Attention";
            else label = "Stable";

            return new DeptTrend(
                    d.getName(),
                    Math.round(thisW * 100.0) / 100.0,
                    Math.round(lastW * 100.0) / 100.0,
                    Math.round(pct * 10.0) / 10.0,
                    dir,
                    label
            );
        }).collect(Collectors.toList());

        return new TrendResponse(
                Math.round(thisMonth * 100.0) / 100.0,
                Math.round(changePercent * 10.0) / 10.0,
                direction,
                weeklyTrend,
                wastePeak,
                deptTrends
        );
    }
}
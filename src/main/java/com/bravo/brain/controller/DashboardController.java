package com.bravo.brain.controller;

import com.bravo.brain.model.dto.DashboardDto.*;
import com.bravo.brain.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    // GET /api/dashboard?store=Bravo Koroglu&departmentId=1
    // REGIONAL_MANAGER login olub departmentId-ni özü bilir
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @RequestParam String store,
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(dashboardService.getDashboard(store, departmentId));
    }
}
package com.bravo.brain.controller;

import com.bravo.brain.model.dto.DirectorDashboardDto.*;
import com.bravo.brain.service.DirectorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/director")
@RequiredArgsConstructor
public class DirectorDashboardController {

    private final DirectorDashboardService directorDashboardService;

    // GET /api/director/dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<DirectorDashboardResponse> getDashboard() {
        return ResponseEntity.ok(directorDashboardService.getDirectorDashboard());
    }
}
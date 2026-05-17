package com.bravo.brain.controller;

import com.bravo.brain.model.dto.TrendDto.*;
import com.bravo.brain.service.TrendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trends")
@RequiredArgsConstructor
public class TrendController {

    private final TrendService trendService;

    // GET /api/trends?store=Bravo Koroğlu&departmentId=1
    @GetMapping
    public ResponseEntity<TrendResponse> getTrends(
            @RequestParam String store,
            @RequestParam Long departmentId) {
        return ResponseEntity.ok(trendService.getTrends(store, departmentId));
    }
}
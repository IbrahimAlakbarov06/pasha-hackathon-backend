package com.bravo.brain.controller;

import com.bravo.brain.model.dto.WasteDto;
import com.bravo.brain.service.WasteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/waste")
@RequiredArgsConstructor
public class WasteController {

    private final WasteService wasteService;

    // Real-time estimated loss — Android quantity dəyişdirdikcə çağırır
    // POST /api/waste/estimate
    @PostMapping("/estimate")
    public ResponseEntity<WasteDto.EstimateResponse> estimate(
            @Valid @RequestBody WasteDto.EstimateRequest req) {
        return ResponseEntity.ok(wasteService.estimate(req));
    }

    // Confirm Log düyməsi
    // POST /api/waste/log
    @PostMapping("/log")
    public ResponseEntity<WasteDto.LogResponse> logWaste(
            @Valid @RequestBody WasteDto.LogRequest req,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(wasteService.logWaste(req, userId));
    }
}
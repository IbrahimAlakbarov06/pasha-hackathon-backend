package com.bravo.brain.controller;

import com.bravo.brain.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // GET /api/reports/waste-pdf?store=Bravo Koroğlu&departmentId=1&period=MONTHLY
    @GetMapping("/waste-pdf")
    public ResponseEntity<byte[]> getWastePdf(
            @RequestParam String store,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "MONTHLY") String period) {

        byte[] pdf = reportService.generateWastePdf(store, departmentId, period);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=freshguard-waste-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
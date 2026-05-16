package com.bravo.brain.controller;

import com.bravo.brain.model.dto.AiDto;
import com.bravo.brain.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @GetMapping("/analyze")
    public ResponseEntity<AiDto.PythonResponse> analyze(
            @RequestParam String store,
            @RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(aiService.analyze(store, departmentId));
    }
}
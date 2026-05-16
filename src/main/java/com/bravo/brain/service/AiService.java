package com.bravo.brain.service;

import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.domain.repository.ProductBatchRepository;
import com.bravo.brain.domain.repository.SaleRepository;
import com.bravo.brain.model.dto.AiDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${freshguard.ai-service-url}")
    private String aiServiceUrl;

    private final ProductBatchRepository batchRepo;
    private final SaleRepository saleRepo;
    private final ObjectMapper objectMapper;

    public AiDto.PythonResponse analyze(String storeName, Long departmentId) {
        try {
            // 1. DB-dən aktiv batchləri gətir (7 günə qədər risk olanlar)
            List<ProductBatch> batches = batchRepo
                    .findAtRisk(LocalDate.now().plusDays(14))
                    .stream()
                    .filter(b -> b.getProduct()
                            .getDepartment().getStoreName().equals(storeName))
                    .filter(b -> departmentId == null ||
                            b.getProduct().getDepartment().getId().equals(departmentId))
                    .collect(Collectors.toList());

            if (batches.isEmpty()) {
                // Batch yoxdursa boş cavab qaytar
                return emptyResponse();
            }

            // 2. Hər batch üçün AI payload hazırla
            List<AiDto.ProductPayload> products = batches.stream()
                    .map(this::buildProductPayload)
                    .collect(Collectors.toList());

            // 3. Tam request obyekti
            AiDto.PythonRequest payload = AiDto.PythonRequest.builder()
                    .storeId(storeName)
                    .date(LocalDate.now().toString())
                    .products(products)
                    .externalFactors(AiDto.ExternalFactors.builder()
                            .weather("normal")
                            .holiday(false)
                            .footfallIndex(1.0)
                            .build())
                    .build();

            // 4. Python AI-ya göndər
            String requestBody = objectMapper.writeValueAsString(payload);
            log.info("AI-ya göndərilir: {} məhsul, mağaza: {}", products.size(), storeName);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(aiServiceUrl))
                    .header("Content-Type", "application/json")
                    .header("ngrok-skip-browser-warning", "true")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("AI servis xətası {}: {}", response.statusCode(), response.body());
                throw new RuntimeException("AI servis xətası: " + response.statusCode());
            }

            return objectMapper.readValue(response.body(), AiDto.PythonResponse.class);

        } catch (Exception e) {
            log.error("AI analiz uğursuz: {}", e.getMessage());
            throw new RuntimeException("AI analiz uğursuz oldu: " + e.getMessage());
        }
    }

    // ── BATCH → AI PAYLOAD ────────────────────────────────
    private AiDto.ProductPayload buildProductPayload(ProductBatch batch) {
        var product = batch.getProduct();

        int expiryDays = (int) ChronoUnit.DAYS.between(
                LocalDate.now(), batch.getRemovalDate());

        // Yalnız son 7 günün ümumi satışından ortalama hesabla
        LocalDateTime since = LocalDateTime.now().minusDays(7);
        Double totalSold = saleRepo.getTotalSoldSince(product.getId(), since);
        double dailySales = totalSold != null ? totalSold / 7.0 : 0.0;

        return AiDto.ProductPayload.builder()
                .name(product.getName())
                .category(product.getCategory().getLabel())
                .stock(batch.getQuantity())
                .expiryDays(Math.max(0, expiryDays))
                .dailySales(dailySales)
                .costPrice(product.getCostPrice() != null ? product.getCostPrice() : 0.0)
                .sellingPrice(product.getSellPrice() != null ? product.getSellPrice() : 0.0)
                .temperatureRisk(false)
                .logisticsDelay(false)
                .build();
    }

    // ── 7 GÜNLÜK ARRAY ────────────────────────────────────
    private List<Double> buildHistoricalArray(List<Object[]> rows) {
        // DB-dən gələn {date, quantity} cütlərini map-ə çevir
        Map<LocalDate, Double> salesMap = new HashMap<>();
        for (Object[] row : rows) {
            LocalDate date = ((java.sql.Date) row[0]).toLocalDate();
            Double qty = ((Number) row[1]).doubleValue();
            salesMap.put(date, qty);
        }

        // Son 7 günü sırayla doldur
        List<Double> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            result.add(salesMap.getOrDefault(day, 0.0));
        }
        return result;
    }

    // ── BOŞ CAVAB ─────────────────────────────────────────
    private AiDto.PythonResponse emptyResponse() {
        AiDto.PythonResponse response = new AiDto.PythonResponse();
        AiDto.Summary summary = new AiDto.Summary();
        summary.setTotalPredictedWasteAzn(0.0);
        summary.setRiskLevel("LOW");
        summary.setKeyIssue("Bu şöbədə risk altında məhsul yoxdur");
        response.setSummary(summary);
        response.setCriticalProducts(new ArrayList<>());
        response.setRecommendations(new ArrayList<>());
        response.setDepartmentProjection(new ArrayList<>());
        return response;
    }
}
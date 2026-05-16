package com.bravo.brain.controller;

import com.bravo.brain.model.dto.ProductDto;
import com.bravo.brain.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // POST /api/products
    @PostMapping
    public ResponseEntity<ProductDto.ProductResponse> createProduct(
            @Valid @RequestBody ProductDto.CreateRequest req) {
        return ResponseEntity.ok(productService.createProduct(req));
    }

    // POST /api/products/batch
    @PostMapping("/batch")
    public ResponseEntity<String> addBatch(
            @Valid @RequestBody ProductDto.AddBatchRequest req) {
        return ResponseEntity.ok(productService.addBatch(req));
    }

    // POST /api/products/sale
    @PostMapping("/sale")
    public ResponseEntity<String> processSale(
            @Valid @RequestBody ProductDto.SaleRequest req) {
        return ResponseEntity.ok(productService.processSale(req));
    }

    // POST /api/products/return
    @PostMapping("/return")
    public ResponseEntity<String> processReturn(
            @Valid @RequestBody ProductDto.ReturnRequest req) {
        return ResponseEntity.ok(productService.processReturn(req));
    }

    // GET /api/products/stock?store=X&department=Y
    @GetMapping("/stock")
    public ResponseEntity<List<ProductDto.StockResponse>> getStock(
            @RequestParam String store,
            @RequestParam String department) {
        return ResponseEntity.ok(productService.getStock(store, department));
    }

    // GET /api/products/barcode/{barcode} — scan endpoint
    @GetMapping("/barcode/{barcode}")
    public ResponseEntity<ProductDto.ProductResponse> getByBarcode(
            @PathVariable String barcode,
            @AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(productService.getByBarcode(barcode, userId));
    }

    // GET /api/products/{id}/barcode-label — çap üçün
    @GetMapping("/{id}/barcode-label")
    public ResponseEntity<ProductDto.BarcodeLabelResponse> getBarcodeLabel(
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getBarcodeLabel(id));
    }
}
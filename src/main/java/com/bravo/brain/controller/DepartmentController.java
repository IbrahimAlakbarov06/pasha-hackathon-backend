package com.bravo.brain.controller;

import com.bravo.brain.model.dto.DepartmentDto.*;
import com.bravo.brain.model.dto.ProductDto;
import com.bravo.brain.service.DepartmentService;
import com.bravo.brain.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;
    private final ProductService productService;

    // POST /api/departments
    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateRequest req) {
        return ResponseEntity.ok(departmentService.createDepartment(req));
    }

    // GET /api/departments?store=Bravo Koroğlu
    @GetMapping
    public ResponseEntity<List<DepartmentResponse>> getByStore(@RequestParam String store) {
        return ResponseEntity.ok(departmentService.getByStore(store));
    }

    // GET /api/departments/{id}/products
    @GetMapping("/{id}/products")
    public ResponseEntity<List<ProductDto.ProductResponse>> getProducts(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getByDepartmentId(id));
    }

    // DELETE /api/departments/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) {
        departmentService.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}
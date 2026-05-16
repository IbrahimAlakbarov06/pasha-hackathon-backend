package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.model.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);
    List<Product> findByStoreName(String storeName);
    List<Product> findByDepartmentName(String departmentName);
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByStoreNameAndDepartmentName(String storeName, String departmentName);
    boolean existsByBarcode(String barcode);
}
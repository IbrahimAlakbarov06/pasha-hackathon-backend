package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.Product;
import com.bravo.brain.model.enums.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByBarcode(String barcode);
    boolean existsByBarcode(String barcode);
    List<Product> findByCategory(ProductCategory category);
    List<Product> findByDepartmentId(Long departmentId);
    List<Product> findByDepartment_StoreName(String storeName);
    List<Product> findByDepartment_StoreNameAndDepartment_Name(String storeName, String departmentName);
}
package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByStoreName(String storeName);
    List<Department> findByStoreNameAndActiveTrue(String storeName);
    boolean existsByNameAndStoreName(String name, String storeName);
}
package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.ProductBatch;
import com.bravo.brain.model.enums.BatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ProductBatchRepository extends JpaRepository<ProductBatch, Long> {

    @Query("SELECT b FROM ProductBatch b WHERE b.product.id = :productId " +
            "AND b.status = 'ACTIVE' AND b.quantity > 0 ORDER BY b.deliveryDate ASC")
    List<ProductBatch> findActiveByProductOrderByDeliveryDate(@Param("productId") Long productId);

    List<ProductBatch> findByRemovalDateAndStatusAndNotified2DayFalse(LocalDate removalDate, BatchStatus status);
    List<ProductBatch> findByRemovalDateAndStatusAndNotified1DayFalse(LocalDate removalDate, BatchStatus status);

    @Query("SELECT b FROM ProductBatch b WHERE b.product.department.name = :dept " +
            "AND b.product.department.storeName = :store AND b.status = 'ACTIVE'")
    List<ProductBatch> findActiveByDepartmentAndStore(@Param("dept") String dept, @Param("store") String store);

    @Query("SELECT b FROM ProductBatch b WHERE b.removalDate <= :date AND b.status = 'ACTIVE' AND b.quantity > 0")
    List<ProductBatch> findAtRisk(@Param("date") LocalDate date);

    Optional<ProductBatch> findByBatchCode(String batchCode);
}
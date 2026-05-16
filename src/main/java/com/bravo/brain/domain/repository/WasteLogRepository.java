package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {

    @Query("SELECT COALESCE(SUM(w.totalLoss), 0) FROM WasteLog w " +
            "WHERE w.product.department.name = :dept AND w.product.department.storeName = :store " +
            "AND w.wasteDate BETWEEN :from AND :to")
    Double getTotalWaste(@Param("dept") String dept, @Param("store") String store,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(w.totalLoss), 0) FROM WasteLog w " +
            "WHERE w.product.department.storeName = :store AND w.wasteDate BETWEEN :from AND :to")
    Double getTotalWasteByStore(@Param("store") String store,
                                @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT w FROM WasteLog w WHERE w.product.department.storeName = :store " +
            "AND w.wasteDate BETWEEN :from AND :to")
    List<WasteLog> findByStoreAndDateBetween(@Param("store") String store,
                                             @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT w FROM WasteLog w WHERE w.product.department.name = :dept " +
            "AND w.wasteDate BETWEEN :from AND :to")
    List<WasteLog> findByDepartmentAndDateBetween(@Param("dept") String dept,
                                                  @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
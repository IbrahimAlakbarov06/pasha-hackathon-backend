package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.WasteLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface WasteLogRepository extends JpaRepository<WasteLog, Long> {

    // Şöbə üzrə ümumi ziyan
    @Query("SELECT COALESCE(SUM(w.totalLoss), 0) FROM WasteLog w " +
            "WHERE w.departmentName = :dept AND w.storeName = :store " +
            "AND w.wasteDate BETWEEN :from AND :to")
    Double getTotalWaste(@Param("dept") String dept, @Param("store") String store,
                         @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Bütün mağaza üzrə ziyan
    @Query("SELECT COALESCE(SUM(w.totalLoss), 0) FROM WasteLog w " +
            "WHERE w.storeName = :store AND w.wasteDate BETWEEN :from AND :to")
    Double getTotalWasteByStore(@Param("store") String store,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to);

    List<WasteLog> findByStoreNameAndWasteDateBetween(
            String storeName, LocalDateTime from, LocalDateTime to);

    List<WasteLog> findByDepartmentNameAndWasteDateBetween(
            String departmentName, LocalDateTime from, LocalDateTime to);
}
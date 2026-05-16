package com.bravo.brain.domain.repository;

import com.bravo.brain.domain.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    // Məhsul üzrə satış sürəti (son N gün)
    @Query("SELECT COALESCE(SUM(s.quantity), 0) FROM Sale s " +
            "WHERE s.product.id = :productId AND s.returned = false " +
            "AND s.saleDate >= :since")
    Double getTotalSoldSince(@Param("productId") Long productId,
                             @Param("since") LocalDateTime since);

    // Şöbə üzrə ümumi satış
    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.departmentName = :dept AND s.storeName = :store " +
            "AND s.returned = false AND s.saleDate BETWEEN :from AND :to")
    Double getTotalRevenue(@Param("dept") String dept, @Param("store") String store,
                           @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    List<Sale> findByProductIdAndReturnedFalseAndSaleDateAfter(
            Long productId, LocalDateTime since);
}
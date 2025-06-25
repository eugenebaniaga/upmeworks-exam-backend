/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.repository;

import com.pos.entity.Sale;
import com.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<Sale, UUID> {
    
    List<Sale> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Sale> findByIdAndUser(UUID id, User user);
    
    @Query("SELECT s FROM Sale s WHERE s.user = :user AND s.createdAt >= :startDate AND s.createdAt <= :endDate ORDER BY s.createdAt DESC")
    List<Sale> findByUserAndCreatedAtBetween(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.user = :user AND s.status = 'COMPLETED'")
    Long countCompletedSalesByUser(@Param("user") User user);
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.user = :user AND s.status = 'COMPLETED'")
    BigDecimal sumTotalAmountByUser(@Param("user") User user);
    
    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.user = :user AND s.status = 'COMPLETED' AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    BigDecimal sumTotalAmountByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(s) FROM Sale s WHERE s.user = :user AND s.status = 'COMPLETED' AND s.createdAt >= :startDate AND s.createdAt <= :endDate")
    Long countSalesByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
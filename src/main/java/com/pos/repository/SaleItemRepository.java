/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.repository;

import com.pos.entity.Product;
import com.pos.entity.SaleItem;
import com.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SaleItemRepository extends JpaRepository<SaleItem, UUID> {
    
    @Query("SELECT si FROM SaleItem si WHERE si.sale.user = :user ORDER BY si.createdAt DESC")
    List<SaleItem> findByUser(@Param("user") User user);
    
    @Query("SELECT si FROM SaleItem si WHERE si.product = :product ORDER BY si.createdAt DESC")
    List<SaleItem> findByProduct(@Param("product") Product product);
    
    @Query("SELECT SUM(si.quantity) FROM SaleItem si WHERE si.product = :product AND si.sale.status = 'COMPLETED'")
    Integer sumQuantityByProduct(@Param("product") Product product);
    
    @Query("SELECT si FROM SaleItem si WHERE si.sale.user = :user AND si.createdAt >= :startDate AND si.createdAt <= :endDate ORDER BY si.createdAt DESC")
    List<SaleItem> findByUserAndDateRange(@Param("user") User user, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
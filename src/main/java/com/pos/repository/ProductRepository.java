/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.repository;

import com.pos.entity.Product;
import com.pos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    List<Product> findByUserOrderByCreatedAtDesc(User user);
    
    Optional<Product> findByIdAndUser(UUID id, User user);
    
    boolean existsBySkuAndUser(String sku, User user);
    
    boolean existsBySkuAndUserAndIdNot(String sku, User user, UUID id);
    
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.isActive = true ORDER BY p.name ASC")
    List<Product> findActiveProductsByUser(@Param("user") User user);
    
    @Query("SELECT p FROM Product p WHERE p.user = :user AND p.stockQuantity < :threshold ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("user") User user, @Param("threshold") Integer threshold);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.user = :user AND p.isActive = true")
    Long countActiveProductsByUser(@Param("user") User user);
}
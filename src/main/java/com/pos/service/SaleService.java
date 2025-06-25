/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.service;

import com.pos.dto.SaleRequest;
import com.pos.entity.Product;
import com.pos.entity.Sale;
import com.pos.entity.SaleItem;
import com.pos.entity.User;
import com.pos.repository.ProductRepository;
import com.pos.repository.SaleRepository;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class SaleService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        User currentUser = getCurrentUser();
        return saleRepository.findByUserOrderByCreatedAtDesc(currentUser);
    }

    public Sale createSale(SaleRequest request) {
        User currentUser = getCurrentUser();

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (SaleRequest.SaleItemRequest itemRequest : request.getItems()) {
            BigDecimal itemTotal = itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }

        if (request.getCashReceived().compareTo(totalAmount) < 0) {
            throw new RuntimeException("Insufficient cash received");
        }

        BigDecimal changeGiven = request.getCashReceived().subtract(totalAmount);

        Sale sale = new Sale();
        sale.setUser(currentUser);
        //sale.setCashier(currentUser);
        sale.setTotalAmount(totalAmount);
        sale.setCashReceived(request.getCashReceived());
        sale.setChangeGiven(changeGiven);

        // Process sale items and update stock
        for (SaleRequest.SaleItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findByIdAndUser(itemRequest.getProductId(), currentUser)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));

            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            // Update stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            // Create sale item
            SaleItem saleItem = new SaleItem();
            saleItem.setProduct(product);
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPrice(itemRequest.getUnitPrice());
            saleItem.setTotalPrice(itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            sale.addSaleItem(saleItem);
        }

        return saleRepository.save(sale);
    }

    @Transactional(readOnly = true)
    public Sale getSaleById(UUID id) {
        User currentUser = getCurrentUser();
        return saleRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
    }

    @Transactional(readOnly = true)
    public List<Sale> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = getCurrentUser();
        return saleRepository.findByUserAndCreatedAtBetween(currentUser, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Long getTotalSalesCount() {
        User currentUser = getCurrentUser();
        return saleRepository.countCompletedSalesByUser(currentUser);
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        User currentUser = getCurrentUser();
        BigDecimal revenue = saleRepository.sumTotalAmountByUser(currentUser);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = getCurrentUser();
        BigDecimal revenue = saleRepository.sumTotalAmountByUserAndDateRange(currentUser, startDate, endDate);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    @Transactional(readOnly = true)
    public Long getSalesCountByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        User currentUser = getCurrentUser();
        return saleRepository.countSalesByUserAndDateRange(currentUser, startDate, endDate);
    }
}
/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.service;

import com.pos.entity.Product;
import com.pos.entity.Sale;
import com.pos.entity.SaleItem;
import com.pos.entity.User;
import com.pos.repository.ProductRepository;
import com.pos.repository.SaleItemRepository;
import com.pos.repository.SaleRepository;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportService {

    @Autowired
    private SaleRepository saleRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Map<String, Object> getDailyReport(LocalDate date) {
        User currentUser = getCurrentUser();
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByUserAndCreatedAtBetween(currentUser, startOfDay, endOfDay);

        Map<String, Object> report = new HashMap<>();
        report.put("date", date.toString());
        report.put("totalSales", sales.size());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);

        // Calculate total profit for the day
        BigDecimal totalProfit = calculateTotalProfit(sales);
        report.put("totalProfit", totalProfit);

        int totalItems = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .mapToInt(SaleItem::getQuantity)
                .sum();
        report.put("totalItems", totalItems);

        BigDecimal averageSale = sales.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
        report.put("averageSale", averageSale);

        report.put("sales", sales);

        return report;
    }

    public Map<String, Object> getMonthlyReport(int year, int month) {
        User currentUser = getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        LocalDateTime startDateTime = startOfMonth.atStartOfDay();
        LocalDateTime endDateTime = endOfMonth.atTime(LocalTime.MAX);

        List<Sale> sales = saleRepository.findByUserAndCreatedAtBetween(currentUser, startDateTime, endDateTime);

        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("month", month);
        report.put("totalSales", sales.size());

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        report.put("totalRevenue", totalRevenue);

        // Calculate total profit for the month
        BigDecimal totalProfit = calculateTotalProfit(sales);
        report.put("totalProfit", totalProfit);

        int totalItems = sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .mapToInt(SaleItem::getQuantity)
                .sum();
        report.put("totalItems", totalItems);

        BigDecimal averageSale = sales.isEmpty() ? BigDecimal.ZERO :
                totalRevenue.divide(BigDecimal.valueOf(sales.size()), 2, RoundingMode.HALF_UP);
        report.put("averageSale", averageSale);

        report.put("sales", sales);

        return report;
    }

    public Map<String, Object> getProductReport() {
        User currentUser = getCurrentUser();
        List<Product> products = productRepository.findByUserOrderByCreatedAtDesc(currentUser);

        Map<String, Object> report = new HashMap<>();

        // Enhance products with sales data
        List<Map<String, Object>> productReports = products.stream().map(product -> {
            Map<String, Object> productData = new HashMap<>();
            productData.put("id", product.getId());
            productData.put("name", product.getName());
            productData.put("sku", product.getSku());
            productData.put("costPrice", product.getCostPrice());
            productData.put("sellingPrice", product.getSellingPrice());
            productData.put("stockQuantity", product.getStockQuantity());
            productData.put("isActive", product.getIsActive());
            productData.put("profitPerUnit", product.getProfitPerUnit());
            productData.put("profitMarginPercentage", product.getProfitMarginPercentage());

            // Calculate sales data for this product
            Integer totalSold = saleItemRepository.sumQuantityByProduct(product);
            productData.put("totalSold", totalSold != null ? totalSold : 0);

            BigDecimal totalRevenue = product.getSaleItems().stream()
                    .filter(item -> item.getSale().getStatus() == Sale.SaleStatus.COMPLETED)
                    .map(SaleItem::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            productData.put("totalRevenue", totalRevenue);

            // Calculate total profit for this product
            BigDecimal totalProfit = BigDecimal.ZERO;
            if (totalSold != null && totalSold > 0) {
                totalProfit = product.getProfitPerUnit().multiply(BigDecimal.valueOf(totalSold));
            }
            productData.put("totalProfit", totalProfit);

            return productData;
        })
        .sorted(Comparator.comparingInt(p -> (Integer) p.get("totalSold")))
        .toList();

        report.put("products", productReports);
        report.put("totalProducts", products.size());

        long activeProducts = products.stream()
                .mapToLong(p -> p.getIsActive() ? 1 : 0)
                .sum();
        report.put("activeProducts", activeProducts);

        return report;
    }

    public Map<String, Object> getDashboardStats() {
        User currentUser = getCurrentUser();

        Map<String, Object> stats = new HashMap<>();

        Long totalProducts = productRepository.countActiveProductsByUser(currentUser);
        stats.put("totalProducts", totalProducts);

        Long totalSales = saleRepository.countCompletedSalesByUser(currentUser);
        stats.put("totalSales", totalSales);

        BigDecimal totalRevenue = saleRepository.sumTotalAmountByUser(currentUser);
        stats.put("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        BigDecimal todayRevenue = saleRepository.sumTotalAmountByUserAndDateRange(currentUser, startOfDay, endOfDay);
        stats.put("todayRevenue", todayRevenue != null ? todayRevenue : BigDecimal.ZERO);

        Long todaySales = saleRepository.countSalesByUserAndDateRange(currentUser, startOfDay, endOfDay);
        stats.put("todaySales", todaySales);

        List<Sale> todaySalesList = saleRepository.findByUserAndCreatedAtBetween(currentUser, startOfDay, endOfDay);
        BigDecimal todayProfit = calculateTotalProfit(todaySalesList);
        stats.put("todayProfit", todayProfit);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        List<Sale> monthlySales = saleRepository.findByUserAndCreatedAtBetween(currentUser, startOfMonthDateTime, endOfDay);
        BigDecimal monthlyProfit = calculateTotalProfit(monthlySales);
        stats.put("monthlyProfit", monthlyProfit);

        List<Product> lowStockProducts = productRepository.findLowStockProducts(currentUser, 10);
        stats.put("lowStockProducts", lowStockProducts.size());
        stats.put("lowStockList", lowStockProducts);
        return stats;
    }

    private BigDecimal calculateTotalProfit(List<Sale> sales) {
        return sales.stream()
                .flatMap(sale -> sale.getItems().stream())
                .map(saleItem -> {
                    Product product = saleItem.getProduct();
                    if (product.getCostPrice() != null) {
                        BigDecimal profitPerUnit = saleItem.getUnitPrice().subtract(product.getCostPrice());
                        return profitPerUnit.multiply(BigDecimal.valueOf(saleItem.getQuantity()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
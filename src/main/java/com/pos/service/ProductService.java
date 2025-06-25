/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/
package com.pos.service;

import com.pos.dto.ProductRequest;
import com.pos.entity.Product;
import com.pos.entity.User;
import com.pos.repository.ProductRepository;
import com.pos.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductService {

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
    public List<Product> getAllProducts() {
        User currentUser = getCurrentUser();
        return productRepository.findByUserOrderByCreatedAtDesc(currentUser);
    }

    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        User currentUser = getCurrentUser();
        return productRepository.findActiveProductsByUser(currentUser);
    }

    @Transactional(readOnly = true)
    public List<Product> getLowStockProducts(Integer threshold) {
        User currentUser = getCurrentUser();
        return productRepository.findLowStockProducts(currentUser, threshold);
    }

    public Product createProduct(ProductRequest request) {
        User currentUser = getCurrentUser();

        if (productRepository.existsBySkuAndUser(request.getSku(), currentUser)) {
            throw new RuntimeException("SKU already exists");
        }

        Product product = new Product();
        product.setUser(currentUser);
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setIsActive(request.getIsActive());

        return productRepository.save(product);
    }

    public Product updateProduct(UUID id, ProductRequest request) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (productRepository.existsBySkuAndUserAndIdNot(request.getSku(), currentUser, id)) {
            throw new RuntimeException("SKU already exists");
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setCostPrice(request.getCostPrice());
        product.setSellingPrice(request.getSellingPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setSku(request.getSku());
        product.setIsActive(request.getIsActive());

        return productRepository.save(product);
    }

    public void deleteProduct(UUID id) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        productRepository.delete(product);
    }

    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        User currentUser = getCurrentUser();
        return productRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product updateStock(UUID productId, Integer newStock) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndUser(productId, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setStockQuantity(newStock);
        return productRepository.save(product);
    }

    public Product reduceStock(UUID productId, Integer quantity) {
        User currentUser = getCurrentUser();
        Product product = productRepository.findByIdAndUser(productId, currentUser)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (product.getStockQuantity() < quantity) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Long getActiveProductCount() {
        User currentUser = getCurrentUser();
        return productRepository.countActiveProductsByUser(currentUser);
    }
}
/**
 ▄▄▄▄▄▄ Dev < Eugene Baniaga >
 ▓ 🦖 ▒ Debian GNU/Linux 12 (bookworm)
 ▀▀▀▀▀▀ Kernel: 6.1.0-32-amd64
**/

package com.pos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class SaleRequest {
    @NotEmpty
    private List<SaleItemRequest> items;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal cashReceived;

    // Constructors
    public SaleRequest() {}

    public SaleRequest(List<SaleItemRequest> items, BigDecimal cashReceived) {
        this.items = items;
        this.cashReceived = cashReceived;
    }

    // Getters and Setters
    public List<SaleItemRequest> getItems() { return items; }
    public void setItems(List<SaleItemRequest> items) { this.items = items; }

    public BigDecimal getCashReceived() { return cashReceived; }
    public void setCashReceived(BigDecimal cashReceived) { this.cashReceived = cashReceived; }

    public static class SaleItemRequest {
        @NotNull
        private UUID productId;

        @NotNull
        @Min(1)
        private Integer quantity;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        private BigDecimal unitPrice;

        // Constructors
        public SaleItemRequest() {}

        public SaleItemRequest(UUID productId, Integer quantity, BigDecimal unitPrice) {
            this.productId = productId;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters and Setters
        public UUID getProductId() { return productId; }
        public void setProductId(UUID productId) { this.productId = productId; }

        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }

        public BigDecimal getUnitPrice() { return unitPrice; }
        public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    }
}
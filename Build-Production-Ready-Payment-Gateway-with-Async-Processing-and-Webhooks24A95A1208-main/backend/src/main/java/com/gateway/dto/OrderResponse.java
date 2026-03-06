package com.gateway.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String customerEmail;
    private LocalDateTime createdAt;
}
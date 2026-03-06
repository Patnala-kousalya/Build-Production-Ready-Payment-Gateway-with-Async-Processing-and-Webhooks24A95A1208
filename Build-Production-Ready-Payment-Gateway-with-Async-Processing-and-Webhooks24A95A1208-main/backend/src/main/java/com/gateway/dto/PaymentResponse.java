package com.gateway.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentResponse {
    private String paymentId;
    private String orderId;
    private BigDecimal amount;
    private String currency;
    private String method;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
}
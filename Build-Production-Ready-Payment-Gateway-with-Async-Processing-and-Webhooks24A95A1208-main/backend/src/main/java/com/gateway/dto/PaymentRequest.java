package com.gateway.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    @NotBlank
    private String orderId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private PaymentMethod method;

    public enum PaymentMethod {
        UPI, CARD
    }
}
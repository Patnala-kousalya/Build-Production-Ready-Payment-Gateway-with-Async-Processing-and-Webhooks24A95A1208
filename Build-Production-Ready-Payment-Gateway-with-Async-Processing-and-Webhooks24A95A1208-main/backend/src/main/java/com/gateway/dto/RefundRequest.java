package com.gateway.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RefundRequest {
    @NotNull
    @DecimalMin(value = "0.01", inclusive = false)
    private BigDecimal amount;
}
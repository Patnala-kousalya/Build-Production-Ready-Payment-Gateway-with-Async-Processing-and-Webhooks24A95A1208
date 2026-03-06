package com.gateway.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WebhookConfigRequest {
    @NotBlank
    private String webhookUrl;
}
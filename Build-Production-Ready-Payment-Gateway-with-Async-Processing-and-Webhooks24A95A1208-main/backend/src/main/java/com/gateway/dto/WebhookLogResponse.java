package com.gateway.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class WebhookLogResponse {
    private Long id;
    private String event;
    private String status;
    private Integer attempts;
    private LocalDateTime lastAttemptAt;
    private Integer responseCode;
}
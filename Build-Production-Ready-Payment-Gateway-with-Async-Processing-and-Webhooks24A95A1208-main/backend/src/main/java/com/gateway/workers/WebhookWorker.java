package com.gateway.workers;

import com.gateway.services.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WebhookWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final WebhookService webhookService;

    @Value("${app.role:API}")
    private String appRole;

    @Scheduled(fixedDelay = 1000)
    public void processWebhooks() {
        if (!"WORKER".equals(appRole)) return;
        Long webhookLogId = (Long) redisTemplate.opsForList().rightPop("webhook_queue");
        if (webhookLogId != null) {
            webhookService.processWebhook(webhookLogId);
        }
    }
}
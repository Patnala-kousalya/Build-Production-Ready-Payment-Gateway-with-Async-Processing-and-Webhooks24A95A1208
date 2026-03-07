package com.gateway.services;

import com.gateway.entities.Merchant;
import com.gateway.entities.WebhookLog;
import com.gateway.repositories.WebhookLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class WebhookService {

    private final WebhookLogRepository webhookLogRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendWebhook(Merchant merchant, String event, Object payload) {
        if (merchant.getWebhookUrl() == null || merchant.getWebhookSecret() == null) {
            return;
        }

        WebhookLog log = new WebhookLog();
        log.setMerchant(merchant);
        log.setEvent(event);
        log.setPayload(toJson(payload));
        log.setStatus(WebhookLog.WebhookStatus.PENDING);
        log.setAttempts(0);
        log.setNextRetryAt(LocalDateTime.now());

        log = webhookLogRepository.save(log);

        // Enqueue webhook job
        redisTemplate.opsForList().leftPush("webhook_queue", log.getId());
    }

    public void processWebhook(Long webhookLogId) {
        WebhookLog log = webhookLogRepository.findById(webhookLogId).orElse(null);
        if (log == null || log.getStatus() != WebhookLog.WebhookStatus.PENDING) {
            return;
        }

        Merchant merchant = log.getMerchant();
        String signature = generateSignature(log.getPayload(), merchant.getWebhookSecret());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("X-Webhook-Signature", signature);

        HttpEntity<String> entity = new HttpEntity<>(log.getPayload(), headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    merchant.getWebhookUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            log.setStatus(WebhookLog.WebhookStatus.SUCCESS);
            log.setResponseCode(response.getStatusCode().value());
            log.setResponseBody(response.getBody());

        } catch (Exception e) {
            log.setStatus(WebhookLog.WebhookStatus.FAILED);
            log.setResponseCode(0);
            log.setResponseBody(e.getMessage());

            // Schedule retry
            log.setAttempts(log.getAttempts() + 1);
            if (log.getAttempts() < 5) {
                LocalDateTime nextRetry = LocalDateTime.now().plusMinutes((long) Math.pow(2, log.getAttempts() - 1));
                log.setNextRetryAt(nextRetry);
                // Re-enqueue
                redisTemplate.opsForList().leftPush("webhook_queue", log.getId());
            }
        }

        log.setLastAttemptAt(LocalDateTime.now());
        webhookLogRepository.save(log);
    }

    private String generateSignature(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to generate signature", e);
        }
    }

    private String toJson(Object payload) {
        // Simple JSON - use Jackson in real app
        return "{}";
    }
}
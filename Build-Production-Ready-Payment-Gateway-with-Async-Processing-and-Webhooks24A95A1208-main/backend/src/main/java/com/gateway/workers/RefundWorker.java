package com.gateway.workers;

import com.gateway.entities.Refund;
import com.gateway.services.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class RefundWorker {

    private final RedisTemplate<Object, Object> redisTemplate;
    private final RefundService refundService;

    @Value("${app.role:API}")
    private String appRole;

    @Scheduled(fixedDelay = 1000)
    public void processRefunds() {
        if (!"WORKER".equals(appRole)) return;
        Long refundId = (Long) redisTemplate.opsForList().rightPop("refund_queue");
        if (refundId != null) {
            Refund refund = refundService.getRefundById(refundId);
            if (refund != null && refund.getStatus() == Refund.RefundStatus.PENDING) {
                // Simulate processing delay
                try {
                    Thread.sleep(new Random().nextInt(3000) + 3000); // 3-5 seconds
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                refundService.updateRefundStatus(refund.getId(), Refund.RefundStatus.PROCESSED);
            }
        }
    }
}
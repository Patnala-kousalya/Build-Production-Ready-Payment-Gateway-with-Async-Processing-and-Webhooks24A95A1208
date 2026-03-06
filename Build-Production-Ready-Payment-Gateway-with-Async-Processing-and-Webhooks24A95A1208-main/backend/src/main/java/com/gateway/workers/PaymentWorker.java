package com.gateway.workers;

import com.gateway.entities.Payment;
import com.gateway.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class PaymentWorker {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PaymentService paymentService;

    @Value("${app.role:API}")
    private String appRole;

    @Scheduled(fixedDelay = 1000) // Check every second
    public void processPayments() {
        if (!"WORKER".equals(appRole)) return;
        Long paymentId = (Long) redisTemplate.opsForList().rightPop("payment_queue");
        if (paymentId != null) {
            // Simulate processing delay
            try {
                Thread.sleep(new Random().nextInt(5000) + 5000); // 5-10 seconds
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            // Simulate success/failure based on method
            Payment payment = paymentService.getPaymentById(paymentId);
            if (payment != null && payment.getStatus() == Payment.PaymentStatus.PENDING) {
                boolean success = new Random().nextDouble() < (payment.getMethod() == Payment.PaymentMethod.UPI ? 0.9 : 0.95);
                Payment.PaymentStatus status = success ? Payment.PaymentStatus.SUCCESS : Payment.PaymentStatus.FAILED;

                paymentService.updatePaymentStatus(payment.getId(), status);
            }
        }
    }
}
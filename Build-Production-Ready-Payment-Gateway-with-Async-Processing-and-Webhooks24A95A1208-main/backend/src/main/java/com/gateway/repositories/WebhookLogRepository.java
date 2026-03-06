package com.gateway.repositories;

import com.gateway.entities.WebhookLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface WebhookLogRepository extends JpaRepository<WebhookLog, Long> {
    List<WebhookLog> findByMerchantIdOrderByCreatedAtDesc(Long merchantId);

    @Query("SELECT w FROM WebhookLog w WHERE w.status = 'FAILED' AND w.nextRetryAt <= :now ORDER BY w.nextRetryAt ASC")
    List<WebhookLog> findFailedWebhooksForRetry(@Param("now") LocalDateTime now);
}
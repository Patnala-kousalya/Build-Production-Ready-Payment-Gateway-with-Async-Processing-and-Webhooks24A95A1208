package com.gateway.config;

import com.gateway.entities.Merchant;
import com.gateway.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final MerchantRepository merchantRepository;

    @Override
    public void run(String... args) throws Exception {
        if (merchantRepository.findByEmail("test@example.com").isEmpty()) {
            Merchant merchant = new Merchant();
            merchant.setEmail("test@example.com");
            merchant.setApiKey("key_test_abc123");
            merchant.setApiSecret("secret_test_xyz789");
            merchant.setWebhookSecret("webhook_secret_xyz");
            merchantRepository.save(merchant);
        }
    }
}
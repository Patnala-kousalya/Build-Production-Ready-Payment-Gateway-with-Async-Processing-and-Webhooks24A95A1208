package com.gateway.services;

import com.gateway.entities.Merchant;
import com.gateway.repositories.MerchantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public Merchant authenticate(String apiKey, String apiSecret) {
        return merchantRepository.findByApiKey(apiKey)
                .filter(merchant -> merchant.getApiSecret().equals(apiSecret))
                .orElse(null);
    }

    public Merchant getMerchantByApiKey(String apiKey) {
        return merchantRepository.findByApiKey(apiKey).orElse(null);
    }

    public void saveMerchant(Merchant merchant) {
        merchantRepository.save(merchant);
    }
}
package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;
import org.example.model.Store;
import org.example.repository.StoreRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final StoreRepository storeRepository;
    private final RiskCalculationService riskCalculationService;

    @Cacheable(value = "stores", key = "#city", unless = "#result == null")
    @Transactional(readOnly = true)
    public List<Store> getStoresByCity(String city) {
        log.info("Fetching stores from DB for city: {}", city);
        return storeRepository.findByCity(city);
    }

    @Transactional(readOnly = true)
    public List<RiskScore> getRisksForCity(String city) {
        List<Store> stores = storeRepository.findByCity(city);
        return stores.stream()
                .map(s -> riskCalculationService.calculateRisk(s.getLatitude(), s.getLongitude()))
                .toList();
    }
}
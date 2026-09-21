package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Store;
import org.example.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/*
 * service for managing stores.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;

    @Transactional(readOnly = true)
    public List<Store> findAll() {
        return storeRepository.findAll();
    }

    @Transactional
    public Store save(Store store) {
        return storeRepository.save(store);
    }

    @Transactional
    public void delete(Long id) {
        storeRepository.deleteById(id);
    }
}
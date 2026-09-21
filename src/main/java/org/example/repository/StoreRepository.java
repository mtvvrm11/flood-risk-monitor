package org.example.repository;

import org.example.model.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 * jpa repository for stores.
 */
@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    List<Store> findByCity(String city);

    List<Store> findByRegion(String region);
}
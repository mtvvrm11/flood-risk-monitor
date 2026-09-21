package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * jpa entity for stores.
 */
@Entity
@Table(name = "stores", indexes = {
        @Index(name = "idx_lat_lon", columnList = "latitude, longitude"),
        @Index(name = "idx_region", columnList = "region"),
        @Index(name = "idx_risk_level", columnList = "current_risk_level")
})
@Data
@NoArgsConstructor
public class Store {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(length = 50)
    private String region;

    @Column(name = "manager_email", nullable = false)
    private String managerEmail;

    @Column(name = "manager_phone")
    private String managerPhone;

    @Column(name = "current_risk_level")
    private Integer currentRiskLevel;

    @Column(name = "last_risk_calculation")
    private LocalDateTime lastRiskCalculation;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
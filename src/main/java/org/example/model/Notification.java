package org.example.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
 * jpa entity for sent notifications.
 */
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_store_sent_at", columnList = "store_id, sent_at DESC")
})
@Data
@NoArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_id", nullable = false)
    private Long storeId;

    @Column(name = "risk_score", nullable = false)
    private Integer riskScore;

    @Column(name = "sent_via", nullable = false, length = 20)
    private String sentVia;

    @Column(name = "recipient", nullable = false, length = 200)
    private String recipient;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "message_content", columnDefinition = "TEXT")
    private String messageContent;
}
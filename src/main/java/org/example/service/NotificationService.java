package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;
import org.example.notification.Notifier;

import java.util.List;

/*
 * dispatches notifications to all configured channels.
 * uses threshold to skip low risks.
 */
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private static final int ALERT_THRESHOLD = 60;

    private final List<Notifier> notifiers;

    /*
     * sends notification if risk is above threshold.
     */
    public void notifyIfNeeded(RiskScore score) {
        if (score.getRiskPercent() < ALERT_THRESHOLD) {
            log.debug("risk {}% below threshold, no notification", score.getRiskPercent());
            return;
        }

        log.info("risk {}% above threshold, sending notifications", score.getRiskPercent());

        for (Notifier notifier : notifiers) {
            try {
                notifier.send(score);
            } catch (Exception e) {
                log.error("notifier {} failed: {}", notifier.getName(), e.getMessage());
            }
        }
    }
}
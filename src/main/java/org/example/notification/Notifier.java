package org.example.notification;

import org.example.model.RiskScore;

/*
 * strategy interface for notification channels.
 */
public interface Notifier {

    void send(RiskScore score);

    void send(String city, RiskScore score);

    String getName();
}
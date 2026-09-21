package org.example.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Slf4j
public class SmsNotifier implements Notifier {

    private final String accountSid;
    private final String authToken;
    private final String fromNumber;
    private final String toNumber;
    private final boolean enabled;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public SmsNotifier(String accountSid, String authToken,
                       String fromNumber, String toNumber, boolean enabled) {
        this.accountSid = accountSid;
        this.authToken = authToken;
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.enabled = enabled;
    }

    @Override
    public void send(RiskScore score) {
        send("Unknown location", score);
    }

    @Override
    public void send(String city, RiskScore score) {
        if (!enabled) {
            log.debug("SMS notifications disabled, skipping");
            return;
        }

        if (score.getRiskPercent() < 80) {
            log.debug("Risk {}% below SMS threshold (80%), skipping", score.getRiskPercent());
            return;
        }

        if (accountSid == null || accountSid.isBlank()) {
            log.warn("SMS not configured, skipping");
            return;
        }

        try {
            String message = String.format(
                    "FLOOD RISK %s: %d%% at (%.4f, %.4f). %s",
                    city,
                    score.getRiskPercent(),
                    score.getLatitude(),
                    score.getLongitude(),
                    getStatusText(score.getRiskColor())
            );

            String url = String.format(
                    "https://api.twilio.com/2010-04-01/Accounts/%s/Messages.json",
                    accountSid
            );

            String body = String.format(
                    "From=%s&To=%s&Body=%s",
                    URLEncoder.encode(fromNumber, StandardCharsets.UTF_8),
                    URLEncoder.encode(toNumber, StandardCharsets.UTF_8),
                    URLEncoder.encode(message, StandardCharsets.UTF_8)
            );

            String auth = Base64.getEncoder().encodeToString(
                    (accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Basic " + auth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() == 201) {
                log.info("SMS sent to {}", toNumber);
            } else {
                log.error("SMS provider returned status {}", response.statusCode());
            }

        } catch (Exception e) {
            log.error("Failed to send SMS: {}", e.getMessage());
        }
    }

    private String getStatusText(String color) {
        return switch (color) {
            case "GREEN" -> "All good.";
            case "YELLOW" -> "Be careful.";
            case "RED" -> "Take action!";
            default -> "";
        };
    }

    @Override
    public String getName() {
        return "SMS";
    }
}
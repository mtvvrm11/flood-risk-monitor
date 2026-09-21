package org.example;

import org.example.model.Coordinates;
import org.example.model.RiskScore;
import org.example.notification.ConsoleNotifier;
import org.example.notification.Notifier;
import org.example.notification.TelegramNotifier;
import org.example.provider.GeoProvider;
import org.example.provider.HistoricalProvider;
import org.example.provider.WeatherProvider;
import org.example.provider.impl.OpenElevationProvider;
import org.example.provider.impl.OpenMeteoProvider;
import org.example.provider.impl.OpenWeatherMapProvider;
import org.example.service.NotificationService;
import org.example.service.RiskCalculationService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*
 * main facade for the flood risk monitor library.
 * provides builder api for api key and telegram.
 */
public class FloodRiskMonitor {

    private final RiskCalculationService calculationService;
    private final NotificationService notificationService;

    private FloodRiskMonitor(String apiKey, String telegramBotToken, String telegramChatId) {
        WeatherProvider weatherProvider = new OpenWeatherMapProvider(apiKey);
        GeoProvider geoProvider = new OpenElevationProvider();
        HistoricalProvider historicalProvider = new OpenMeteoProvider();

        this.calculationService = new RiskCalculationService(
                weatherProvider, geoProvider, historicalProvider
        );

        List<Notifier> notifiers = new ArrayList<>();
        notifiers.add(new ConsoleNotifier());
        if (telegramBotToken != null && telegramChatId != null) {
            notifiers.add(new TelegramNotifier(telegramBotToken, telegramChatId));
        }
        this.notificationService = new NotificationService(notifiers);
    }

    public static Builder builder() {
        return new Builder();
    }

    public RiskScore calculateRisk(double latitude, double longitude) {
        validateCoordinates(latitude, longitude);
        return calculationService.calculateRisk(latitude, longitude);
    }

    public RiskScore calculateAndNotify(double latitude, double longitude) {
        RiskScore score = calculateRisk(latitude, longitude);
        notificationService.notifyIfNeeded(score);
        return score;
    }

    public List<RiskScore> calculateRiskBatch(List<Coordinates> coordinates) {
        return coordinates.stream()
                .map(c -> calculateRisk(c.getLatitude(), c.getLongitude()))
                .collect(Collectors.toList());
    }

    private void validateCoordinates(double latitude, double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }
    }

    /*
     * builder for flood risk monitor.
     */
    public static class Builder {
        private String apiKey;
        private String telegramBotToken;
        private String telegramChatId;

        public Builder withApiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder withTelegram(String botToken, String chatId) {
            this.telegramBotToken = botToken;
            this.telegramChatId = chatId;
            return this;
        }

        public FloodRiskMonitor build() {
            if (apiKey == null || apiKey.isBlank()) {
                throw new IllegalStateException("api key is required");
            }
            return new FloodRiskMonitor(apiKey, telegramBotToken, telegramChatId);
        }
    }
}
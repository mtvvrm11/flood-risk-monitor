package org.example.scheduler;

import lombok.extern.slf4j.Slf4j;
import org.example.FloodRiskMonitor;
import org.example.model.RiskScore;
import org.example.notification.EmailNotifier;
import org.example.notification.Notifier;
import org.example.notification.TelegramNotifier;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/*
 * scheduled task that checks flood risk for configured cities every 2 hours.
 * sends notifications via all configured channels.
 */
@Slf4j
public class FloodRiskScheduler {

    private static final Map<String, double[]> CITIES = new LinkedHashMap<>() {{
        put("london, united kingdom", new double[]{51.5074, -0.1278});
        put("edinburgh, united kingdom", new double[]{55.9533, -3.1883});
        put("brighton, united kingdom", new double[]{50.8225, -0.1372});
    }};

    private static final long INTERVAL_HOURS = 2;
    private static final long PAUSE_BETWEEN_CITIES_MS = 2000;

    private final FloodRiskMonitor monitor;
    private final List<Notifier> notifiers;

    public FloodRiskScheduler(FloodRiskMonitor monitor,
                              String botToken, String chatId,
                              EmailNotifier emailNotifier) {
        this.monitor = monitor;

        this.notifiers = new ArrayList<>();
        this.notifiers.add(new TelegramNotifier(botToken, chatId));

        if (emailNotifier != null) {
            this.notifiers.add(emailNotifier);
        }
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        log.info("starting flood risk scheduler for {} cities (every {} hours)",
                CITIES.size(), INTERVAL_HOURS);

        checkAllCities();

        scheduler.scheduleAtFixedRate(
                this::checkAllCities,
                INTERVAL_HOURS,
                INTERVAL_HOURS,
                TimeUnit.HOURS
        );

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("shutting down scheduler...");
            scheduler.shutdown();
        }));
    }

    /*
     * iterates cities and sends notifications.
     */
    private void checkAllCities() {
        log.info("=== scheduled check for {} cities ===", CITIES.size());

        String header = String.format(
                "flood risk monitor - scheduled check\n" +
                        "time: %s",
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        );

        for (Notifier notifier : notifiers) {
            if (notifier instanceof TelegramNotifier telegram) {
                telegram.sendMessage(header);
            }
        }

        for (Map.Entry<String, double[]> entry : CITIES.entrySet()) {
            String city = entry.getKey();
            double[] coords = entry.getValue();

            try {
                log.info("checking {}: lat={}, lon={}", city, coords[0], coords[1]);

                RiskScore score = monitor.calculateRisk(coords[0], coords[1]);

                log.info("{} risk: {}% ({})", city, score.getRiskPercent(), score.getRiskColor());

                for (Notifier notifier : notifiers) {
                    try {
                        notifier.send(city, score);
                    } catch (Exception e) {
                        log.error("notifier {} failed: {}", notifier.getName(), e.getMessage());
                    }
                }

                Thread.sleep(PAUSE_BETWEEN_CITIES_MS);

            } catch (Exception e) {
                log.error("failed to check {}: {}", city, e.getMessage(), e);
            }
        }

        log.info("=== all cities checked ===");
    }
}
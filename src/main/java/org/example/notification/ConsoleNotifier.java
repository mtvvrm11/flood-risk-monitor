package org.example.notification;

import lombok.extern.slf4j.Slf4j;
import org.example.model.RiskScore;

/*
 * console notification channel.
 * prints risk summary to standard output.
 */
@Slf4j
public class ConsoleNotifier implements Notifier {

    @Override
    public void send(RiskScore score) {
        send("unknown location", score);
    }

    @Override
    public void send(String city, RiskScore score) {
        String emoji = getEmoji(score.getRiskColor());
        System.out.printf(
                "%s %s - risk: %d%% (%s) at (%.4f, %.4f)%n",
                emoji,
                city,
                score.getRiskPercent(),
                score.getRiskLevel(),
                score.getLatitude(),
                score.getLongitude()
        );
    }

    private String getEmoji(String color) {
        return switch (color) {
            case "GREEN" -> "🌿";
            case "YELLOW" -> "🌙";
            case "RED" -> "🌊⭐";
            default -> "?";
        };
    }

    @Override
    public String getName() {
        return "CONSOLE";
    }
}
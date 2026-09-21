package org.example.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/*
 * result of risk calculation.
 * contains percentage, color, and components.
 */
@Data
@Builder
public class RiskScore {

    private double latitude;
    private double longitude;
    private int riskPercent;
    private String riskColor;
    private double historicalRisk;
    private double geoRisk;
    private double weatherRisk;
    private LocalDateTime calculatedAt;
    private String source;

    /*
     * returns emoji for risk color.
     */
    public String getColorEmoji() {
        return switch (riskColor) {
            case "RED" -> "🌊⭐";
            case "YELLOW" -> "🌙";
            case "GREEN" -> "🌿";
            default -> "?";
        };
    }

    /*
     * returns risk level as text.
     */
    public String getRiskLevel() {
        if (riskPercent >= 80) return "CRITICAL";
        if (riskPercent >= 60) return "HIGH";
        if (riskPercent >= 40) return "MEDIUM";
        return "LOW";
    }
}
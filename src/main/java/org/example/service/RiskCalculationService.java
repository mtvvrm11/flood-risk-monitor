package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.*;
import org.example.provider.GeoProvider;
import org.example.provider.HistoricalProvider;
import org.example.provider.WeatherProvider;

import java.time.LocalDateTime;

/*
 * core service for calculating flood risk.
 * formula: (historical * 0.4) + (geo * 0.3) + (weather * 0.3).
 */
@Slf4j
@RequiredArgsConstructor
public class RiskCalculationService {

    private static final double HISTORICAL_WEIGHT = 0.4;
    private static final double GEO_WEIGHT = 0.3;
    private static final double WEATHER_WEIGHT = 0.3;

    private final WeatherProvider weatherProvider;
    private final GeoProvider geoProvider;
    private final HistoricalProvider historicalProvider;

    /*
     * calculates risk for given coordinates.
     */
    public RiskScore calculateRisk(double latitude, double longitude) {
        log.debug("calculating risk for lat={}, lon={}", latitude, longitude);

        WeatherData weather = weatherProvider.getWeather(latitude, longitude);
        GeoData geo = geoProvider.getGeoData(latitude, longitude);
        HistoricalData history = historicalProvider.getHistoricalData(latitude, longitude);

        double historicalRisk = calculateHistoricalRisk(history);
        double geoRisk = calculateGeoRisk(geo);
        double weatherRisk = calculateWeatherRisk(weather);

        double weightedRisk = historicalRisk * HISTORICAL_WEIGHT
                + geoRisk * GEO_WEIGHT
                + weatherRisk * WEATHER_WEIGHT;

        int riskPercent = (int) Math.round(weightedRisk * 100);
        riskPercent = Math.max(0, Math.min(100, riskPercent));

        String color = determineColor(riskPercent);
        String source = determineSource(weather, geo, history);

        RiskScore score = RiskScore.builder()
                .latitude(latitude)
                .longitude(longitude)
                .riskPercent(riskPercent)
                .riskColor(color)
                .historicalRisk(historicalRisk)
                .geoRisk(geoRisk)
                .weatherRisk(weatherRisk)
                .calculatedAt(LocalDateTime.now())
                .source(source)
                .build();

        log.info("risk calculated: {}% ({}) for lat={}, lon={}",
                riskPercent, color, latitude, longitude);

        return score;
    }

    private double calculateHistoricalRisk(HistoricalData history) {
        double eventRisk = Math.min(history.getFloodEventsLast10Years() / 5.0, 1.0);
        double levelRisk = Math.min(history.getAverageFloodLevel(), 1.0);
        return (eventRisk + levelRisk) / 2.0;
    }

    private double calculateGeoRisk(GeoData geo) {
        double risk = 0.0;
        if (geo.getElevation() < 5.0) risk += 0.7;
        else if (geo.getElevation() < 10.0) risk += 0.4;
        else if (geo.getElevation() < 20.0) risk += 0.2;
        else risk += 0.05;

        if (geo.getDistanceToRiver() < 100.0) risk += 0.3;
        else if (geo.getDistanceToRiver() < 500.0) risk += 0.15;

        return Math.min(risk, 1.0);
    }

    private double calculateWeatherRisk(WeatherData weather) {
        double risk = 0.0;
        if (weather.getRainfall() > 50.0) risk += 0.5;
        else if (weather.getRainfall() > 25.0) risk += 0.3;
        else if (weather.getRainfall() > 10.0) risk += 0.1;

        if (weather.getWindSpeed() > 20.0) risk += 0.3;
        else if (weather.getWindSpeed() > 10.0) risk += 0.1;

        return Math.min(risk, 1.0);
    }

    private String determineColor(int riskPercent) {
        if (riskPercent >= 80) return "RED";
        if (riskPercent >= 60) return "YELLOW";
        return "GREEN";
    }

    private String determineSource(WeatherData weather, GeoData geo, HistoricalData history) {
        boolean anyFallback = "FALLBACK".equals(weather.getSource())
                || "FALLBACK".equals(geo.getSource())
                || "FALLBACK".equals(history.getSource());
        if (anyFallback) return "PARTIAL_FALLBACK";
        return weather.getSource() + "+" + geo.getSource() + "+" + history.getSource();
    }
}
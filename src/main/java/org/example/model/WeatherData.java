package org.example.model;

import lombok.Builder;
import lombok.Data;

/*
 * weather data from external api.
 */
@Data
@Builder
public class WeatherData {

    private double temperature;
    private double rainfall;
    private double windSpeed;
    private double humidity;
    private String source;

    /*
     * returns fallback data when api is unavailable.
     */
    public static WeatherData fallback() {
        return WeatherData.builder()
                .temperature(15.0)
                .rainfall(5.0)
                .windSpeed(10.0)
                .humidity(70.0)
                .source("FALLBACK")
                .build();
    }
}
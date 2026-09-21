package org.example.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.FloodRiskException;
import org.example.model.WeatherData;
import org.example.provider.WeatherProvider;
import org.springframework.web.client.RestTemplate;

/*
 * weather provider using openweathermap api.
 * free tier: 1000 calls per day.
 */
@Slf4j
public class OpenWeatherMapProvider implements WeatherProvider {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiKey;

    public OpenWeatherMapProvider(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public WeatherData getWeather(double latitude, double longitude) {
        try {
            return fetchWeather(latitude, longitude);
        } catch (Exception e) {
            log.error("failed to fetch weather: {}", e.getMessage());
            return WeatherData.fallback();
        }
    }

    /*
     * sends http request and parses json response.
     */
    private WeatherData fetchWeather(double latitude, double longitude) throws Exception {
        String url = String.format("%s?lat=%f&lon=%f&appid=%s&units=metric",
                BASE_URL, latitude, longitude, apiKey);

        log.info("fetching weather from openweathermap: lat={}, lon={}", latitude, longitude);

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);

        return WeatherData.builder()
                .temperature(root.path("main").path("temp").asDouble())
                .humidity(root.path("main").path("humidity").asDouble())
                .rainfall(root.path("rain").path("1h").asDouble(0.0))
                .windSpeed(root.path("wind").path("speed").asDouble())
                .source("OpenWeatherMap")
                .build();
    }

    @Override
    public String getName() {
        return "OpenWeatherMap";
    }
}
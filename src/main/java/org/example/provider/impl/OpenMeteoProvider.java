package org.example.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.model.HistoricalData;
import org.example.provider.HistoricalProvider;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

/*
 * historical data provider using open-meteo archive api.
 */
@Slf4j
public class OpenMeteoProvider implements HistoricalProvider {

    private static final String BASE_URL = "https://archive-api.open-meteo.com/v1/archive";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public HistoricalData getHistoricalData(double latitude, double longitude) {
        try {
            return fetchHistorical(latitude, longitude);
        } catch (Exception e) {
            log.error("failed to fetch historical data: {}", e.getMessage());
            return HistoricalData.fallback();
        }
    }

    /*
     * fetches precipitation history for the last year.
     */
    private HistoricalData fetchHistorical(double latitude, double longitude) throws Exception {
        LocalDate endDate = LocalDate.now().minusDays(1);
        LocalDate startDate = endDate.minusYears(1);

        String url = String.format(
                "%s?latitude=%f&longitude=%f&start_date=%s&end_date=%s&daily=precipitation_sum",
                BASE_URL, latitude, longitude, startDate, endDate
        );

        log.info("fetching historical data from open-meteo: lat={}, lon={}", latitude, longitude);

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);
        JsonNode daily = root.path("daily").path("precipitation_sum");

        double totalRainfall = 0.0;
        int rainyDays = 0;

        if (daily.isArray()) {
            for (JsonNode value : daily) {
                double precipitation = value.asDouble();
                totalRainfall += precipitation;
                if (precipitation > 10.0) rainyDays++;
            }
        }

        int floodEvents = rainyDays / 10;

        return HistoricalData.builder()
                .floodEventsLast10Years(floodEvents)
                .averageFloodLevel(totalRainfall / 365.0 / 50.0)
                .source("Open-Meteo")
                .build();
    }

    @Override
    public String getName() {
        return "Open-Meteo";
    }
}
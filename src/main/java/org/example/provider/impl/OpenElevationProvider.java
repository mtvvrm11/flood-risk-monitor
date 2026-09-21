package org.example.provider.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.FloodRiskException;
import org.example.model.GeoData;
import org.example.provider.GeoProvider;
import org.springframework.web.client.RestTemplate;

/*
 * elevation provider using open-meteo elevation api.
 */
@Slf4j
public class OpenElevationProvider implements GeoProvider {

    private static final String BASE_URL = "https://api.open-meteo.com/v1/elevation";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public GeoData getGeoData(double latitude, double longitude) {
        try {
            return fetchElevation(latitude, longitude);
        } catch (Exception e) {
            log.error("failed to fetch elevation: {}", e.getMessage());
            return GeoData.fallback();
        }
    }

    /*
     * fetches elevation and estimates distance to river.
     */
    private GeoData fetchElevation(double latitude, double longitude) throws Exception {
        String url = String.format("%s?latitude=%f&longitude=%f", BASE_URL, latitude, longitude);

        log.info("fetching elevation from open-meteo: lat={}, lon={}", latitude, longitude);

        String response = restTemplate.getForObject(url, String.class);
        JsonNode root = objectMapper.readTree(response);

        double elevation = 0.0;
        JsonNode elevationNode = root.path("elevation");
        if (elevationNode.isArray() && elevationNode.size() > 0) {
            elevation = elevationNode.get(0).asDouble();
        }

        double distanceToRiver = estimateDistanceToRiver(elevation);
        String floodZone = determineFloodZone(elevation, distanceToRiver);

        return GeoData.builder()
                .elevation(elevation)
                .distanceToRiver(distanceToRiver)
                .floodZone(floodZone)
                .source("Open-Meteo-Elevation")
                .build();
    }

    /*
     * estimates distance to river based on elevation.
     */
    private double estimateDistanceToRiver(double elevation) {
        if (elevation < 5.0) return 50.0;
        if (elevation < 15.0) return 200.0;
        if (elevation < 50.0) return 500.0;
        if (elevation < 200.0) return 1000.0;
        return 2000.0;
    }

    private String determineFloodZone(double elevation, double distanceToRiver) {
        if (elevation < 5.0 || distanceToRiver < 100.0) return "HIGH_RISK";
        if (elevation < 15.0 || distanceToRiver < 500.0) return "MEDIUM_RISK";
        return "LOW_RISK";
    }

    @Override
    public String getName() {
        return "Open-Meteo-Elevation";
    }
}
package org.example.model;

import lombok.Builder;
import lombok.Data;

/*
 * geographical data from external api.
 */
@Data
@Builder
public class GeoData {

    private double elevation;
    private double distanceToRiver;
    private String floodZone;
    private String source;

    /*
     * returns fallback data when api is unavailable.
     */
    public static GeoData fallback() {
        return GeoData.builder()
                .elevation(10.0)
                .distanceToRiver(500.0)
                .floodZone("LOW_RISK")
                .source("FALLBACK")
                .build();
    }
}
package org.example.model;

import lombok.Builder;
import lombok.Data;

/*
 * historical flood data.
 */
@Data
@Builder
public class HistoricalData {

    private int floodEventsLast10Years;
    private double averageFloodLevel;
    private String source;

    /*
     * returns fallback data when api is unavailable.
     */
    public static HistoricalData fallback() {
        return HistoricalData.builder()
                .floodEventsLast10Years(0)
                .averageFloodLevel(0.5)
                .source("FALLBACK")
                .build();
    }
}
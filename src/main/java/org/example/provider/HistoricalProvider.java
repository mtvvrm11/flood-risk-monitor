package org.example.provider;

import org.example.model.HistoricalData;

/*
 * interface for historical flood data providers.
 */
public interface HistoricalProvider {

    HistoricalData getHistoricalData(double latitude, double longitude);

    String getName();
}
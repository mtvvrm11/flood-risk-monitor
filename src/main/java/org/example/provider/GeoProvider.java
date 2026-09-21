package org.example.provider;

import org.example.model.GeoData;

/*
 * interface for geographical data providers.
 */
public interface GeoProvider {

    GeoData getGeoData(double latitude, double longitude);

    String getName();
}
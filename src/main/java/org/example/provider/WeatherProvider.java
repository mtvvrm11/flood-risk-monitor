package org.example.provider;

import org.example.model.WeatherData;

/*
 * interface for weather data providers.
 */
public interface WeatherProvider {

    WeatherData getWeather(double latitude, double longitude);

    String getName();
}
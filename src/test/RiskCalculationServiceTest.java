package org.example;

import org.example.model.*;
import org.example.provider.GeoProvider;
import org.example.provider.HistoricalProvider;
import org.example.provider.WeatherProvider;
import org.example.service.RiskCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Risk Calculation Service Tests")
class RiskCalculationServiceTest {

    @Mock
    private WeatherProvider weatherProvider;

    @Mock
    private GeoProvider geoProvider;

    @Mock
    private HistoricalProvider historicalProvider;

    private RiskCalculationService service;

    @BeforeEach
    void setUp() {
        service = new RiskCalculationService(weatherProvider, geoProvider, historicalProvider);
    }

    @Test
    @DisplayName("RED risk for dangerous conditions")
    void shouldCalculateRedRisk() {
        when(weatherProvider.getWeather(anyDouble(), anyDouble()))
                .thenReturn(WeatherData.builder().rainfall(60.0).windSpeed(25.0).source("TEST").build());
        when(geoProvider.getGeoData(anyDouble(), anyDouble()))
                .thenReturn(GeoData.builder().elevation(3.0).distanceToRiver(50.0).source("TEST").build());
        when(historicalProvider.getHistoricalData(anyDouble(), anyDouble()))
                .thenReturn(HistoricalData.builder().floodEventsLast10Years(8).averageFloodLevel(0.9).source("TEST").build());

        RiskScore score = service.calculateRisk(51.5, -0.1);

        assertTrue(score.getRiskPercent() >= 80);
        assertEquals("RED", score.getRiskColor());
    }

    @Test
    @DisplayName("GREEN risk for safe conditions")
    void shouldCalculateGreenRisk() {
        when(weatherProvider.getWeather(anyDouble(), anyDouble()))
                .thenReturn(WeatherData.builder().rainfall(2.0).windSpeed(5.0).source("TEST").build());
        when(geoProvider.getGeoData(anyDouble(), anyDouble()))
                .thenReturn(GeoData.builder().elevation(50.0).distanceToRiver(2000.0).source("TEST").build());
        when(historicalProvider.getHistoricalData(anyDouble(), anyDouble()))
                .thenReturn(HistoricalData.builder().floodEventsLast10Years(0).averageFloodLevel(0.0).source("TEST").build());

        RiskScore score = service.calculateRisk(51.5, -0.1);

        assertTrue(score.getRiskPercent() < 40);
        assertEquals("GREEN", score.getRiskColor());
    }

    @Test
    @DisplayName("YELLOW risk for medium conditions")
    void shouldCalculateYellowRisk() {
        when(weatherProvider.getWeather(anyDouble(), anyDouble()))
                .thenReturn(WeatherData.builder().rainfall(30.0).windSpeed(15.0).source("TEST").build());
        when(geoProvider.getGeoData(anyDouble(), anyDouble()))
                .thenReturn(GeoData.builder().elevation(8.0).distanceToRiver(300.0).source("TEST").build());
        when(historicalProvider.getHistoricalData(anyDouble(), anyDouble()))
                .thenReturn(HistoricalData.builder().floodEventsLast10Years(3).averageFloodLevel(0.5).source("TEST").build());

        RiskScore score = service.calculateRisk(51.5, -0.1);

        assertTrue(score.getRiskPercent() >= 40);
        assertTrue(score.getRiskPercent() < 80);
    }
}
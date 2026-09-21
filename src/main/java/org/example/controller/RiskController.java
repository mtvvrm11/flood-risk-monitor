package org.example.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.example.model.Coordinates;
import org.example.model.RiskScore;
import org.example.service.RiskCalculationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
 * rest controller for risk calculation endpoints.
 */
@RestController
@RequestMapping("/api/risks")
@RequiredArgsConstructor
public class RiskController {

    private final RiskCalculationService riskCalculationService;

    @Operation(summary = "get current risk for coordinates")
    @GetMapping("/current")
    public ResponseEntity<RiskScore> getCurrentRisk(
            @RequestParam double latitude,
            @RequestParam double longitude) {
        return ResponseEntity.ok(riskCalculationService.calculateRisk(latitude, longitude));
    }

    @Operation(summary = "get risks for multiple locations")
    @PostMapping("/batch")
    public ResponseEntity<List<RiskScore>> getBatchRisks(
            @RequestBody List<Coordinates> coordinates) {
        List<RiskScore> scores = coordinates.stream()
                .map(c -> riskCalculationService.calculateRisk(c.getLatitude(), c.getLongitude()))
                .toList();
        return ResponseEntity.ok(scores);
    }
}
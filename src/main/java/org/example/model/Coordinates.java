package org.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
 * geographic coordinates.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {

    private double latitude;
    private double longitude;

    public static Coordinates of(double latitude, double longitude) {
        return new Coordinates(latitude, longitude);
    }
}
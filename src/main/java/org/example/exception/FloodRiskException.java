package org.example.exception;

/*
 * base exception for flood risk monitor.
 */
public class FloodRiskException extends RuntimeException {

    public FloodRiskException(String message) {
        super(message);
    }

    public FloodRiskException(String message, Throwable cause) {
        super(message, cause);
    }
}
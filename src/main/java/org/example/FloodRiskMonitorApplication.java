package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/*
 * spring boot entry point.
 */
@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableCaching
public class FloodRiskMonitorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FloodRiskMonitorApplication.class, args);
    }
}
package org.example.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * loads secrets from secrets.properties file in project root.
 * the file must not be committed to git.
 */
public class SecretsLoader {

    private static final Properties PROPERTIES = new Properties();

    static {
        try (InputStream input = new FileInputStream("secrets.properties")) {
            PROPERTIES.load(input);
            System.out.println("secrets loaded from secrets.properties");
        } catch (IOException e) {
            System.err.println("secrets.properties not found, using defaults");
        }
    }

    public static String get(String key) {
        return PROPERTIES.getProperty(key, "");
    }

    public static String get(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }
}
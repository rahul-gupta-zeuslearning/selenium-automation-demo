package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.openqa.selenium.json.Json;

/**
 * Utility to load test data from JSON or Properties files in the testdata/ directory.
 * Uses Selenium's built-in JSON parser (zero external library downloads needed).
 */
public class TestDataUtil {

    private static final String TEST_DATA_DIR = "testdata/";
    private static final Json json = new Json();

    // ==========================================
    // JSON Data Loaders
    // ==========================================

    /**
     * Loads a JSON file from testdata/ into a Map<String, Object>.
     * @param fileName File name with or without .json extension
     * @return Map containing parsed JSON data, or an empty Map if reading fails.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadJsonData(String fileName) {
        String path = TEST_DATA_DIR + fileName;
        if (!path.endsWith(".json")) {
            path += ".json";
        }

        File file = new File(path);
        if (!file.exists()) {
            System.err.println("⚠️ Warning: JSON test data file not found at " + file.getAbsolutePath());
            return new HashMap<>();
        }

        try (FileReader reader = new FileReader(file)) {
            Map<String, Object> data = json.toType(reader, Map.class);
            return (data != null) ? data : new HashMap<>();
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Error loading JSON from " + path + ": " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * Retrieves a value from a JSON file in testdata/, supporting dot-notation for nested keys.
     * E.g. getJsonValue("LoginLogoutTestData", "roles.teacher")
     */
    public static Object getJsonValue(String fileName, String keyPath) {
        Map<String, Object> data = loadJsonData(fileName);
        if (data == null || keyPath == null || keyPath.isEmpty()) {
            return null;
        }

        if (!keyPath.contains(".")) {
            return data.get(keyPath);
        }

        String[] parts = keyPath.split("\\.");
        Object current = data;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }

    /**
     * Gets a String value from a JSON file.
     */
    public static String getJsonString(String fileName, String keyPath) {
        Object val = getJsonValue(fileName, keyPath);
        return (val != null) ? String.valueOf(val) : null;
    }

    /**
     * Gets a String value from a JSON file with a default fallback.
     */
    public static String getJsonString(String fileName, String keyPath, String defaultValue) {
        String val = getJsonString(fileName, keyPath);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }

    // ==========================================
    // Legacy Properties File Loaders (Optional)
    // ==========================================

    public static Properties loadTestData(String fileName) {
        Properties properties = new Properties();
        String path = TEST_DATA_DIR + fileName;
        if (!path.endsWith(".properties")) {
            path += ".properties";
        }

        File file = new File(path);
        if (!file.exists()) {
            return properties;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Error loading test data from " + path + ": " + e.getMessage());
        }

        return properties;
    }

    public static String getProperty(String fileName, String key) {
        return loadTestData(fileName).getProperty(key);
    }

    public static String getProperty(String fileName, String key, String defaultValue) {
        return loadTestData(fileName).getProperty(key, defaultValue);
    }
}

package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Utility to load test data from the testdata/ directory without external libraries.
 */
public class TestDataUtil {

    private static final String TEST_DATA_DIR = "testdata/";

    /**
     * Loads a Properties file from testdata/ folder.
     * @param fileName Name of file with or without .properties extension
     * @return Loaded Properties object
     */
    public static Properties loadTestData(String fileName) {
        Properties properties = new Properties();
        String path = TEST_DATA_DIR + fileName;
        if (!path.endsWith(".properties")) {
            path += ".properties";
        }

        File file = new File(path);
        if (!file.exists()) {
            System.err.println("⚠️ Warning: Test data file not found at " + file.getAbsolutePath());
            return properties;
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            properties.load(fis);
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Error loading test data from " + path + ": " + e.getMessage());
        }

        return properties;
    }

    /**
     * Helper to directly fetch a single property value from a file in testdata/
     */
    public static String getProperty(String fileName, String key) {
        Properties props = loadTestData(fileName);
        return props.getProperty(key);
    }
}

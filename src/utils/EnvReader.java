package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Loads key-value pairs from .env file at the project root without external libraries.
 * Automatically falls back to System environment variables.
 */
public class EnvReader {
    private static final Map<String, String> envVars = new HashMap<>();

    static {
        loadEnvFile(".env");
    }

    private static void loadEnvFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and comment lines
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int delimiterIndex = line.indexOf('=');
                if (delimiterIndex > 0) {
                    String key = line.substring(0, delimiterIndex).trim();
                    String value = line.substring(delimiterIndex + 1).trim();

                    // Strip optional quotes around value
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envVars.put(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("⚠️ Warning: Failed to read " + filePath + ": " + e.getMessage());
        }
    }

    /**
     * Gets value for given key from .env file or system environment variables.
     */
    public static String get(String key) {
        // 1. From .env file
        if (envVars.containsKey(key)) {
            return envVars.get(key);
        }
        // 2. From System environment
        String systemEnv = System.getenv(key);
        if (systemEnv != null && !systemEnv.isEmpty()) {
            return systemEnv;
        }
        // 3. From JVM System properties (-Dkey=value)
        return System.getProperty(key);
    }

    /**
     * Gets value for given key or returns the provided default value if not found.
     */
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isEmpty()) ? val : defaultValue;
    }
}

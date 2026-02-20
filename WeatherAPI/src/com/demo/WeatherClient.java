package com.demo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * WeatherClient - A Java REST API Client
 * Fetches real-time weather data from Open-Meteo (no API key required)
 * and displays it in a structured, formatted output.
 *
 * API Used: https://open-meteo.com/
 */
public class WeatherClient {

    // ─── Configuration ────────────────────────────────────────────────────────
    private static final String BASE_URL = "https://api.open-meteo.com/v1/forecast";

    // Default location: New York City
    private static final double DEFAULT_LAT  = 40.7128;
    private static final double DEFAULT_LON  = -74.0060;
    private static final String DEFAULT_CITY = "New York City";

    // ─── Main Entry Point ─────────────────────────────────────────────────────
    public static void main(String[] args) {
        double latitude  = DEFAULT_LAT;
        double longitude = DEFAULT_LON;
        String cityName  = DEFAULT_CITY;

        // Allow optional CLI args: java WeatherClient <lat> <lon> <city>
        if (args.length >= 3) {
            try {
                latitude  = Double.parseDouble(args[0]);
                longitude = Double.parseDouble(args[1]);
                cityName  = args[2];
            } catch (NumberFormatException e) {
                System.out.println("⚠ Invalid coordinates. Using default location.");
            }
        }

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║         Java REST API Weather Client             ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        try {
            String jsonResponse = fetchWeatherData(latitude, longitude);
            Map<String, String> weatherData = parseWeatherJson(jsonResponse);
            displayWeatherReport(cityName, latitude, longitude, weatherData);
        } catch (Exception e) {
            System.out.println("✗ Error fetching weather data: " + e.getMessage());
            System.out.println();
            System.out.println("Tip: Make sure you have an active internet connection.");
            e.printStackTrace();
        }
    }

    // ─── HTTP Request ─────────────────────────────────────────────────────────
    /**
     * Sends an HTTP GET request to the Open-Meteo API and returns the raw JSON response.
     */
    private static String fetchWeatherData(double lat, double lon) throws Exception {
        String params = String.format(
            "?latitude=%.4f&longitude=%.4f" +
            "&current=temperature_2m,relative_humidity_2m,apparent_temperature," +
            "precipitation,wind_speed_10m,wind_direction_10m,weather_code,surface_pressure" +
            "&temperature_unit=celsius&wind_speed_unit=kmh&timezone=auto",
            lat, lon
        );

        String fullUrl = BASE_URL + params;
        System.out.println("► Connecting to: " + fullUrl);
        System.out.println();

        URL url = new URL(fullUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);

        int responseCode = connection.getResponseCode();
        System.out.println("► HTTP Response Code: " + responseCode + " " + connection.getResponseMessage());
        System.out.println();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("API request failed with HTTP code: " + responseCode);
        }

        // Read the response body
        StringBuilder responseBody = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                responseBody.append(line);
            }
        }

        connection.disconnect();
        return responseBody.toString();
    }

    // ─── JSON Parser ──────────────────────────────────────────────────────────
    /**
     * Lightweight manual JSON parser — no external libraries required.
     * Extracts values from the "current" block of the Open-Meteo response.
     */
    private static Map<String, String> parseWeatherJson(String json) {
        Map<String, String> data = new LinkedHashMap<>();

        // Extract top-level fields
        data.put("timezone",   extractJsonValue(json, "timezone"));
        data.put("elevation",  extractJsonValue(json, "elevation"));
        data.put("time",       extractJsonValue(json, "time"));

        // Extract current weather fields
        data.put("temperature",          extractJsonValue(json, "temperature_2m"));
        data.put("feels_like",           extractJsonValue(json, "apparent_temperature"));
        data.put("humidity",             extractJsonValue(json, "relative_humidity_2m"));
        data.put("precipitation",        extractJsonValue(json, "precipitation"));
        data.put("wind_speed",           extractJsonValue(json, "wind_speed_10m"));
        data.put("wind_direction",       extractJsonValue(json, "wind_direction_10m"));
        data.put("pressure",             extractJsonValue(json, "surface_pressure"));
        data.put("weather_code",         extractJsonValue(json, "weather_code"));

        return data;
    }

    /**
     * Extracts a JSON field value by key name (handles both string and numeric values).
     */
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return "N/A";

        int colonIndex = json.indexOf(":", keyIndex + searchKey.length());
        if (colonIndex == -1) return "N/A";

        // Skip whitespace after colon
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;

        if (valueStart >= json.length()) return "N/A";

        char firstChar = json.charAt(valueStart);

        // String value
        if (firstChar == '"') {
            int endQuote = json.indexOf("\"", valueStart + 1);
            return endQuote == -1 ? "N/A" : json.substring(valueStart + 1, endQuote);
        }

        // Numeric / boolean / null value
        int valueEnd = valueStart;
        while (valueEnd < json.length()) {
            char c = json.charAt(valueEnd);
            if (c == ',' || c == '}' || c == ']' || c == '\n') break;
            valueEnd++;
        }
        return json.substring(valueStart, valueEnd).trim();
    }

    // ─── Display ──────────────────────────────────────────────────────────────
    /**
     * Prints the weather data in a structured, human-readable report.
     */
    private static void displayWeatherReport(String city, double lat, double lon,
                                              Map<String, String> data) {
        String weatherDesc = describeWeatherCode(data.getOrDefault("weather_code", "0"));
        String windDir     = describeWindDirection(data.getOrDefault("wind_direction", "0"));

        System.out.println("┌─────────────────────────────────────────────────┐");
        System.out.printf( "│  📍 Location : %-33s│%n", city);
        System.out.printf( "│  🌐 Timezone : %-33s│%n", data.getOrDefault("timezone", "N/A"));
        System.out.printf( "│  🗺  Coords   : Lat %-6.2f  Lon %-14.2f│%n", lat, lon);
        System.out.printf( "│  🏔  Elevation: %-29s m │%n", data.getOrDefault("elevation", "N/A"));
        System.out.printf( "│  🕐 Time     : %-33s│%n", data.getOrDefault("time", "N/A"));
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│               CURRENT CONDITIONS                │");
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.printf( "│  🌤  Weather  : %-33s│%n", weatherDesc);
        System.out.printf( "│  🌡  Temp     : %-29s °C │%n", data.getOrDefault("temperature", "N/A"));
        System.out.printf( "│  🤔 Feels    : %-29s °C │%n", data.getOrDefault("feels_like", "N/A"));
        System.out.printf( "│  💧 Humidity : %-30s %% │%n", data.getOrDefault("humidity", "N/A"));
        System.out.printf( "│  🌧  Precip   : %-27s mm  │%n", data.getOrDefault("precipitation", "N/A"));
        System.out.printf( "│  🌬  Wind     : %-24s km/h  │%n",
            data.getOrDefault("wind_speed", "N/A") + " from " + windDir);
        System.out.printf( "│  📊 Pressure : %-24s hPa   │%n", data.getOrDefault("pressure", "N/A"));
        System.out.println("├─────────────────────────────────────────────────┤");
        System.out.println("│  Data source : Open-Meteo (open-meteo.com)      │");
        System.out.println("│  No API key required — Free & Open Source       │");
        System.out.println("└─────────────────────────────────────────────────┘");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    /**
     * Maps WMO Weather Codes to human-readable descriptions.
     * Reference: https://open-meteo.com/en/docs#weathervariables
     */
    private static String describeWeatherCode(String codeStr) {
        try {
            int code = (int) Double.parseDouble(codeStr);
            if (code == 0)            return "Clear sky ☀";
            if (code == 1)            return "Mainly clear 🌤";
            if (code == 2)            return "Partly cloudy ⛅";
            if (code == 3)            return "Overcast ☁";
            if (code >= 45 && code <= 48) return "Fog 🌫";
            if (code >= 51 && code <= 55) return "Drizzle 🌦";
            if (code >= 61 && code <= 65) return "Rain 🌧";
            if (code >= 71 && code <= 75) return "Snow ❄";
            if (code == 77)           return "Snow grains 🌨";
            if (code >= 80 && code <= 82) return "Rain showers 🌦";
            if (code >= 85 && code <= 86) return "Snow showers 🌨";
            if (code >= 95 && code <= 99) return "Thunderstorm ⛈";
            return "Unknown (code " + code + ")";
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }

    /**
     * Converts wind direction degrees to cardinal/intercardinal compass label.
     */
    private static String describeWindDirection(String degreesStr) {
        try {
            double deg = Double.parseDouble(degreesStr);
            String[] dirs = {"N","NNE","NE","ENE","E","ESE","SE","SSE",
                             "S","SSW","SW","WSW","W","WNW","NW","NNW"};
            int index = (int) Math.round(deg / 22.5) % 16;
            return dirs[index] + " (" + (int) deg + "°)";
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }
}
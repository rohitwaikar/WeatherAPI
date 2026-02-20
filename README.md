# WeatherAPI

**Company Name** CodeTech It Solutions

**Name:** Rohit Waikar

**Project :** Weather Rest API 

**Intern Id:** CTIS4163

**Domain Name:** Java Programming

**Mentor Name** Neela Santosh

#Description :

WeatherClient is a pure Java console application that connects to a live public REST API (Open-Meteo), retrieves real-time weather data for any location on Earth, parses the JSON response without any external libraries, and displays the results in a clean, formatted report.
To demonstrate how Java handles the full REST API lifecycle — making an HTTP request, receiving a JSON response, extracting data from it, and presenting it in a readable structure — using only the standard Java library (no Maven, no Gradle, no third-party dependencies).
The program is organized into four focused methods, each handling one responsibility:
main() — The entry point. Sets the default location (New York City), accepts optional command-line arguments for custom coordinates, and orchestrates the three steps: fetch → parse → display.
fetchWeatherData(lat, lon) — Builds the API URL with query parameters, opens an HttpURLConnection, sets the request method to GET, checks the HTTP response code, reads the response body line by line using a BufferedReader, and returns the raw JSON string.
parseWeatherJson(json) — Calls extractJsonValue() for each field of interest and stores the results in a LinkedHashMap, preserving insertion order for display.
extractJsonValue(json, key) — A lightweight manual JSON parser. It searches for a key by name, locates the colon after it, then reads the value — handling both quoted string values and bare numeric/boolean values — without using any JSON library.
displayWeatherReport() — Formats all extracted data into a structured Unicode box-table printed to the console, calling two helper methods:

describeWeatherCode() — maps WMO integer codes (0–99) to human-readable labels like "Partly cloudy ⛅" or "Thunderstorm ⛈"
describeWindDirection() — converts wind direction in degrees (0–360) to a 16-point compass label like "NW (315°)"
<img width="1282" height="709" alt="Image" src="https://github.com/user-attachments/assets/df1ad96b-b3ab-4ca8-86e7-49dc8025063f" />

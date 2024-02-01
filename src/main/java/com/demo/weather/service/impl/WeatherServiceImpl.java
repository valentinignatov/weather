package com.demo.weather.service.impl;

import com.demo.weather.model.Weather;
import com.demo.weather.model.WeatherHistoryBean;
import com.demo.weather.repo.WeatherRepo;
import com.demo.weather.service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final String apiKey = readConfigFile();
    private static final String ZIP_REGEXP = "^\\d{5}(?:[-\\s]\\d{4})?$";
    private static final String QUOTES_COLON_REGEXP = "\"([^\"]*)\"|:(\\s*\"([^\"]*)\")|:(\\s*([^\"\\s:{}]+))";

    private final String WEATHER_API_URL =
            "http://api.openweathermap.org/data/2.5/weather?zip={zipCode}&appid={apiKey}&units=metric"; //90210

    private final RestTemplate restTemplate;
    private final WeatherRepo weatherRepo;

    public WeatherServiceImpl(RestTemplate restTemplate, WeatherRepo weatherRepo) {
        this.restTemplate = restTemplate;
        this.weatherRepo = weatherRepo;
    }

    public Optional<Weather> getWeatherByPostalCode(String postalCode) {
        try {
            if (isValidPostalCode(postalCode)) {

                String apiUrl = WEATHER_API_URL.replace("{zipCode}", postalCode).replace("{apiKey}", apiKey);
                String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

                List<String> extractedText = extractTextQuotesOrColon(jsonResponse);

                Weather weather = extractRelevantData(extractedText);

                WeatherHistoryBean weatherHistory = WeatherHistoryBean.builder()
                        .postalCode(postalCode)
                        .condition(weather.getCondition())
                        .temp(weather.getTemp())
                        .humidity(weather.getHumidity())
                        .country(weather.getCountry())
                        .city(weather.getCity())
                        .visibility(weather.getVisibility())
                        .windSpeed(weather.getWindSpeed())
                        .requestTime(formatTimeString(Timestamp.from(ZonedDateTime.now().toInstant())))
                        .build();

                weatherRepo.save(weatherHistory);

                System.out.println(weatherHistory);

                return Optional.of(weather);
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Postal code has incorrect format");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Error fetching weather information. City not found");
        }
    }

    public List<WeatherHistoryBean> getWeatherHistoryByPostalCode(String postalCode) {
        return weatherRepo.getWeatherHistoryBeanByPostalCode(postalCode)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Weather info not found"));
    }

    private boolean isValidPostalCode(String postalCode) {
        Pattern pattern = Pattern.compile(ZIP_REGEXP);
        Matcher matcher = pattern.matcher(postalCode);
        return matcher.matches();
    }

    private List<String> extractTextQuotesOrColon(String input) {
        List<String> extractedText = new ArrayList<>();
        Pattern pattern = Pattern.compile(QUOTES_COLON_REGEXP);
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            if (matcher.group(1) != null) {
                // Text inside quotes
                extractedText.add(matcher.group(1));
            } else if (matcher.group(3) != null) {
                // Text after colon and inside quotes
                extractedText.add(matcher.group(3));
            } else if (matcher.group(5) != null) {
                // Text after colon and not in quotes
                extractedText.add(matcher.group(5));
            }
        }

        return extractedText;
    }

    private Weather extractRelevantData(List<String> extractedText) {

        Weather weather = new Weather();

        for (int i = 0; i < extractedText.size(); i++) {
            if (extractedText.get(i).equalsIgnoreCase("description")) {
                weather.setCondition(extractedText.get(i + 1));
            }
            if (extractedText.get(i).equalsIgnoreCase("temp")) {
                weather.setTemp(excludeCharacters(extractedText.get(i + 1), ","));
            }
            if (extractedText.get(i).equalsIgnoreCase("humidity")) {
                weather.setHumidity(excludeCharacters(extractedText.get(i + 1), ","));
            }
            if (extractedText.get(i).equalsIgnoreCase("country")) {
                weather.setCountry(extractedText.get(i + 1));
            }
            if (extractedText.get(i).equalsIgnoreCase("name")) {
                weather.setCity(extractedText.get(i + 1));
            }
            if (extractedText.get(i).equalsIgnoreCase("visibility")) {
                weather.setVisibility(excludeCharacters(extractedText.get(i + 1), ","));
            }
            if (extractedText.get(i).equalsIgnoreCase("speed")) {
                weather.setWindSpeed(excludeCharacters(extractedText.get(i + 1), ","));
            }
        }
        return weather;
    }

    private String excludeCharacters(String inputString, String charactersToExclude) {
        StringBuilder input = new StringBuilder(inputString);
        int inputLength = input.length();

        for (int i = 0; i < inputLength; i++) {
            char currentChar = input.charAt(i);

            if (charactersToExclude.indexOf(currentChar) == -1) {
                input.append(currentChar);
            }
        }

        input.delete(0, inputLength);

        return input.toString();
    }

    private Timestamp formatTime(Timestamp originalTimestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTimestampStr = sdf.format(originalTimestamp);
        Date formattedDate = null;
        try {
            formattedDate = sdf.parse(formattedTimestampStr);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Timestamp parsedTimestamp = new Timestamp(formattedDate.getTime());
        return parsedTimestamp;
    }

    private String formatTimeString(Timestamp originalTimestamp) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Create a SimpleDateFormat object with the desired date and time format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Format the Timestamp using the SimpleDateFormat
        return sdf.format(timestamp);
    }

    private String readConfigFile() {
        try {
            // Get the current working directory
            String currentDirectory = System.getProperty("user.dir");

            // Specify the file path relative to the root of the project
            Path filePath = Paths.get(currentDirectory, "weatherApiKey.txt");

            // Read the content of the file
            byte[] content = Files.readAllBytes(filePath);
            return new String(content);
        } catch (IOException e) {
            // Handle exception (e.g., log it or throw a custom exception)
            e.printStackTrace();
            return null;
        }
    }
}

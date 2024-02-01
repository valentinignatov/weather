package com.demo.weather.service;

import com.demo.weather.model.Weather;
import com.demo.weather.model.WeatherHistoryBean;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface WeatherService {
    Optional<Weather> getWeatherByPostalCode(String postalCode);

    List<WeatherHistoryBean> getWeatherHistoryByPostalCode(String postalCode);
}

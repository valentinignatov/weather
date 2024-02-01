package com.demo.weather.rest;

import com.demo.weather.model.Weather;
import com.demo.weather.model.WeatherHistoryBean;
import com.demo.weather.model.WeatherRequestBean;
import com.demo.weather.service.impl.WeatherServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "mydomain/app")
public class WeatherController {

    WeatherServiceImpl weatherService;

    public WeatherController(WeatherServiceImpl weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping(path = "/history")
    public ResponseEntity<List<WeatherHistoryBean>> getWeatherHistoryByPostalCode(
            @RequestParam String postalcode) {
        return new ResponseEntity<>(weatherService.getWeatherHistoryByPostalCode(postalcode), HttpStatus.OK);
    }

    @PostMapping(path = "/weather")
    public ResponseEntity<Weather> getWeatherByPostalCode(
            @RequestBody WeatherRequestBean weatherRequestBean) {
        return new ResponseEntity<>(weatherService.getWeatherByPostalCode(weatherRequestBean.getPostalcode()).get(), HttpStatus.OK);
    }

}
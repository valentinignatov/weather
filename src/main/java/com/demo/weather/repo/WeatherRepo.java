package com.demo.weather.repo;

import com.demo.weather.model.WeatherHistoryBean;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherRepo extends CrudRepository<WeatherHistoryBean, Integer> {
    Optional<List<WeatherHistoryBean>> getWeatherHistoryBeanByPostalCode(String postalCode);

}

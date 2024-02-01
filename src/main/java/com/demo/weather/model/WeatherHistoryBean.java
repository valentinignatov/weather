package com.demo.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity
@Table(name = "weather_history")
public class WeatherHistoryBean {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;
    private String postalCode;
    private String condition;
    private String temp;
    private String humidity;
    private String country;
    private String city;
    private String visibility;
    private String windSpeed;
    private String requestTime;
}

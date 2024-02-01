package com.demo.weather;

import com.demo.weather.model.WeatherHistoryBean;
import com.demo.weather.model.WeatherRequestBean;
import com.demo.weather.repo.WeatherRepo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
class WeatherControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WeatherRepo weatherRepo;

    @Test
    void getWeatherByPostalCode() throws Exception {
        WeatherRequestBean requestBean = new WeatherRequestBean();
        requestBean.setPostalcode("90210");

        byte[] requestBeanJson = toJson(requestBean);

        MvcResult resultCreate = mockMvc.perform(post("/mydomain/app/weather")
                        .content(requestBeanJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.condition", Matchers.notNullValue()))
                .andExpect(jsonPath("$.temp", Matchers.notNullValue()))
                .andExpect(jsonPath("$.humidity", Matchers.notNullValue()))
                .andExpect(jsonPath("$.country", Matchers.notNullValue()))
                .andExpect(jsonPath("$.city", Matchers.notNullValue()))
                .andExpect(jsonPath("$.visibility", Matchers.notNullValue()))
                .andExpect(jsonPath("$.windSpeed", Matchers.notNullValue()))
                .andReturn();

        Optional<List<WeatherHistoryBean>> weatherHistoryBeanByPostalCode = weatherRepo.getWeatherHistoryBeanByPostalCode("90210");

        Assert.assertNotNull(weatherHistoryBeanByPostalCode.get().get(0));
    }

    @Test
    void getWeatherHistoryByPostalCode() throws Exception {

        WeatherHistoryBean weatherHistory = WeatherHistoryBean.builder()
                .postalCode("90210")
                .condition("test")
                .temp("test")
                .humidity("test")
                .country("test")
                .city("test")
                .visibility("test")
                .windSpeed("test")
                .requestTime(formatTimeString(Timestamp.from(ZonedDateTime.now().toInstant())))
                .build();

        weatherRepo.save(weatherHistory);

        MvcResult resultCreate = mockMvc.perform(get("/mydomain/app/history")
                .param("postalcode", "90210"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$[0].id", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].postalCode", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].condition", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].temp", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].humidity", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].country", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].city", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].visibility", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].windSpeed", Matchers.notNullValue()))
                .andExpect(jsonPath("$[0].requestTime", Matchers.notNullValue()))
                .andReturn();
    }

    private byte[] toJson(Object object) throws JsonProcessingException {
        ObjectMapper map = new ObjectMapper();
        return map.writeValueAsString(object).getBytes();
    }

    private String formatTimeString(Timestamp originalTimestamp) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        // Create a SimpleDateFormat object with the desired date and time format
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        // Format the Timestamp using the SimpleDateFormat
        return sdf.format(timestamp);
    }
}

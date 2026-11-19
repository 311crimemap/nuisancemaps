package com.quirkshop.nuisancemaps.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quirkshop.nuisancemaps.NuisancemapsApplication;
import com.quirkshop.nuisancemaps.dto.PropertiesDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

@SpringBootTest(classes = NuisancemapsApplication.class)
class JsonCompatibilityTest {
    @Autowired ObjectMapper mapper;
    @Autowired RestTemplate restTemplate;
    @Autowired RequestMappingHandlerAdapter handlerAdapter;

    @Test
    void apiAndHttpClientKeepJackson2ForExistingCustomSerializers() {
        assertThat(handlerAdapter.getMessageConverters().stream()
                .filter(converter -> converter.canWrite(PropertiesDTO.class, MediaType.APPLICATION_JSON))
                .findFirst().orElseThrow())
                .isInstanceOf(MappingJackson2HttpMessageConverter.class);
        assertThat(restTemplate.getMessageConverters().stream()
                .filter(converter -> converter.canWrite(PropertiesDTO.class, MediaType.APPLICATION_JSON))
                .findFirst().orElseThrow())
                .isInstanceOf(MappingJackson2HttpMessageConverter.class);
    }

    @Test
    void geoJsonPropertiesKeepIsoDatesAndExistingFieldNames() throws Exception {
        var properties = new PropertiesDTO("report", "address", "location",
                LocalDateTime.of(2026, 11, 19, 12, 30), "123", null);
        var json = mapper.readTree(mapper.writeValueAsString(properties));
        assertThat(json.get("reportedAt").asText()).isEqualTo("2026-11-19T12:30:00");
        assertThat(json.get("reportNum").asText()).isEqualTo("123");
        assertThat(json.get("reportCategory").asText()).isEqualTo("report");
    }
}

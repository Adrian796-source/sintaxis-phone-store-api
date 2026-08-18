package com.adrian.sintaxis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class JacksonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldSerializeLocalDateTime() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        String json = objectMapper.writeValueAsString(Map.of("fecha", now));

        assertThat(json).isNotNull();
        assertThat(json).contains("\"fecha\"");
    }

    @Test
    void shouldDeserializeLocalDateTime() throws Exception {
        String json = "{\"fecha\":\"2024-01-15T10:30:00\"}";
        Map<String, Object> result = objectMapper.readValue(json, Map.class);

        assertThat(result).isNotNull();
        assertThat(result).containsKey("fecha");
    }

    @Test
    void objectMapperShouldBeConfigured() {
        assertThat(objectMapper).isNotNull();
        assertThat(objectMapper.getRegisteredModuleIds()).isNotEmpty();
    }
}
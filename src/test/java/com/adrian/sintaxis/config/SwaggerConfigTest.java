package com.adrian.sintaxis.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SwaggerConfigTest {

    @Autowired
    private SwaggerConfig swaggerConfig;

    @Test
    void swaggerConfigShouldBeLoaded() {
        assertThat(swaggerConfig).isNotNull();
    }

    @Test
    void shouldCreateOpenAPI() {
        OpenAPI openAPI = swaggerConfig.openAPI();

        assertThat(openAPI).isNotNull();

        // Verificar Info
        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("Sintaxis Phone Store API");
        assertThat(info.getDescription()).isEqualTo("API REST para gestión de tienda de celulares y accesorios");
        assertThat(info.getVersion()).isEqualTo("1.0.0");

        // Verificar Components
        Components components = openAPI.getComponents();
        assertThat(components).isNotNull();
        assertThat(components.getSecuritySchemes()).containsKey("Bearer");

        SecurityScheme securityScheme = components.getSecuritySchemes().get("Bearer");
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void shouldHaveBearerSecurityScheme() {
        OpenAPI openAPI = swaggerConfig.openAPI();
        Components components = openAPI.getComponents();

        SecurityScheme securityScheme = components.getSecuritySchemes().get("Bearer");
        assertThat(securityScheme).isNotNull();
        assertThat(securityScheme.getName()).isEqualTo("Bearer");
        assertThat(securityScheme.getType()).isEqualTo(SecurityScheme.Type.HTTP);
        assertThat(securityScheme.getScheme()).isEqualTo("bearer");
        assertThat(securityScheme.getBearerFormat()).isEqualTo("JWT");
    }

    @Test
    void shouldHaveSecurityRequirement() {
        OpenAPI openAPI = swaggerConfig.openAPI();

        assertThat(openAPI.getSecurity()).isNotEmpty();
        SecurityRequirement requirement = openAPI.getSecurity().get(0);
        assertThat(requirement).isNotNull();
        // ✅ Usar toString() para verificar que contiene "Bearer"
        assertThat(requirement.toString()).contains("Bearer");
    }

    @Test
    void shouldHaveCorrectInfo() {
        OpenAPI openAPI = swaggerConfig.openAPI();
        Info info = openAPI.getInfo();

        assertThat(info.getTitle()).isEqualTo("Sintaxis Phone Store API");
        assertThat(info.getDescription()).isEqualTo("API REST para gestión de tienda de celulares y accesorios");
        assertThat(info.getVersion()).isEqualTo("1.0.0");
    }

    @Test
    void shouldContainBearerSecurity() {
        OpenAPI openAPI = swaggerConfig.openAPI();

        // ✅ Verificar que el SecurityRequirement contiene "Bearer"
        boolean hasBearer = openAPI.getSecurity().stream()
                .anyMatch(req -> req.toString().contains("Bearer"));
        assertThat(hasBearer).isTrue();
    }
}
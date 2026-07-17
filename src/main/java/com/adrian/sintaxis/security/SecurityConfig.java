package com.adrian.sintaxis.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // 🔴 CONFIGURACIÓN CORS CON MÁXIMA SEGURIDAD
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔴 SOLO ORÍGENES ESPECÍFICOS Y CONOCIDOS
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",      // React desarrollo
                "http://localhost:5173",      // Vite desarrollo
                "http://localhost:4200",      // Angular desarrollo
                "https://tudominio.com",      // 🔴 REEMPLAZAR CON TU DOMINIO DE PRODUCCIÓN
                "https://www.tudominio.com"   // 🔴 REEMPLAZAR CON TU DOMINIO DE PRODUCCIÓN
        ));

        // 🔴 SOLO MÉTODOS HTTP QUE REALMENTE USAS
        configuration.setAllowedMethods(Arrays.asList(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH"
        ));

        // 🔴 SOLO HEADERS NECESARIOS
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin"
        ));

        // 🔴 EXPONER HEADERS AL CLIENTE (SOLO LOS NECESARIOS)
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        // 🔴 PERMITIR CREDENCIALES (IMPORTANTE PARA JWT)
        configuration.setAllowCredentials(true);

        // 🔴 TIEMPO DE CACHE PARA PREFLIGHT (1 hora)
        configuration.setMaxAge(3600L);

        // 🔴 APLICAR A TODAS LAS RUTAS
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos de Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", 
                                "/v3/api-docs.yaml", "/webjars/**", "/swagger-resources", "/swagger-resources/**").permitAll()
                        
                        // Endpoints públicos de Auth
                        .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                        .requestMatchers("/api/auth/perfil").authenticated()

                        // Productos - GET público, modificaciones solo ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.GET, "/api/celulares/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/accesorios/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/celulares/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/celulares/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/celulares/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.POST, "/api/accesorios/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.PUT, "/api/accesorios/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers(HttpMethod.DELETE, "/api/accesorios/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Clientes - solo ADMIN
                        .requestMatchers("/api/clientes/**").hasRole("ADMIN")

                        // Ventas - ADMIN y EMPLEADO, mis-ventas para CLIENTE
                        .requestMatchers("/api/ventas/mis-ventas").hasRole("CLIENTE")
                        .requestMatchers("/api/ventas/reportes/**").hasAnyRole("ADMIN", "EMPLEADO")
                        .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "EMPLEADO")

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

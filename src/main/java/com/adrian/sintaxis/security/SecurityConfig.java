package com.adrian.sintaxis.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Constantes para rutas de productos
    private static final String CELULARES_PATH = "/api/celulares/**";
    private static final String ACCESORIOS_PATH = "/api/accesorios/**";

    // 🔐 Constantes para roles con prefijo ROLE_
    private static final String ROLE_ADMIN = "ROLE_ADMIN";
    private static final String ROLE_EMPLEADO = "ROLE_EMPLEADO";
    private static final String ROLE_CLIENTE = "ROLE_CLIENTE";

    private final JwtAuthFilter jwtAuthFilter;
    private final UserDetailsServiceImpl userDetailsService;

    // 🔴 CONFIGURACIÓN CORS CON MÁXIMA SEGURIDAD
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 🔴 SOLO ORÍGENES ESPECÍFICOS Y CONOCIDOS
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",
                "http://localhost:3000",      // React desarrollo
                "http://localhost:5173",      // Vite desarrollo
                "http://localhost:4200",      // Angular desarrollo
                "https://sintaxis-phone-store-api.onrender.com"     // 🔴 DOMINIO DE PRODUCCIÓN
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
    @SuppressWarnings("java:S4502")
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF desactivado porque la API es STATELESS y usa JWT en el header Authorization.
                // No se usan cookies de sesión, por lo que los ataques CSRF no aplican.
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index.html", "/*.css", "/*.js", "/static/**", "/css/**", "/js/**", "/images/**").permitAll()

                        // Endpoints públicos de Swagger/OpenAPI
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs", "/v3/api-docs/**", 
                                "/v3/api-docs.yaml", "/webjars/**", "/swagger-resources", "/swagger-resources/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/subcategorias/**").permitAll()
                        // Endpoints públicos de Auth e Imágenes
                        .requestMatchers("/api/auth/login", "/api/auth/register", "/api/imagenes/**").permitAll()
                        .requestMatchers("/api/auth/perfil").authenticated()
                        .requestMatchers("/api/auth/usuarios").hasAuthority(ROLE_ADMIN)

                        // Productos - GET público, modificaciones solo ADMIN y EMPLEADO
                        .requestMatchers(HttpMethod.GET, CELULARES_PATH).permitAll()
                        .requestMatchers(HttpMethod.GET, ACCESORIOS_PATH).permitAll()

                        .requestMatchers(HttpMethod.POST, CELULARES_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.PUT, CELULARES_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.DELETE, CELULARES_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)

                        .requestMatchers(HttpMethod.POST, ACCESORIOS_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.PUT, ACCESORIOS_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.DELETE, ACCESORIOS_PATH).hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)

                        // Clientes - solo ADMIN
                        .requestMatchers("/api/clientes/**").hasAuthority(ROLE_ADMIN)

                        // Ventas - ADMIN y EMPLEADO, mis-ventas y creación público (para invitados) o autenticado
                        .requestMatchers("/api/ventas/mis-ventas").hasAnyAuthority(ROLE_CLIENTE, ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers("/api/ventas/reportes/**").hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.POST, "/api/ventas").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/ventas/**").hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.PUT, "/api/ventas/**").hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.DELETE, "/api/ventas/**").hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)
                        .requestMatchers(HttpMethod.PATCH, "/api/ventas/**").hasAnyAuthority(ROLE_ADMIN, ROLE_EMPLEADO)

                        // Cualquier otro endpoint requiere autenticación
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
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

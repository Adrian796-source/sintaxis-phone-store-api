package com.adrian.sintaxis.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    private UserDetails userDetails;
    private final String validToken = "valid.jwt.token";
    private final String email = "juan@test.com";

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        userDetails = org.springframework.security.core.userdetails.User
                .withUsername(email)
                .password("password")
                .roles("CLIENTE")
                .build();
    }

    @Test
    void doFilterInternal_ShouldAuthenticateUser_WhenTokenIsValid() throws Exception {
        // ✅ Configurar requestURI para que no sea null
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extraerEmail(validToken)).thenReturn(email);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.esTokenValido(validToken, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenIsBlacklisted() throws Exception {
        // ✅ Configurar requestURI
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(true);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenNoAuthorizationHeader() throws Exception {
        // ✅ Configurar requestURI
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extraerEmail(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenAuthHeaderDoesNotStartWithBearer() throws Exception {
        // ✅ Configurar requestURI
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extraerEmail(anyString());
    }

    @Test
    void doFilterInternal_ShouldNotAuthenticate_WhenTokenIsInvalid() throws Exception {
        // ✅ Configurar requestURI
        when(request.getRequestURI()).thenReturn("/api/test");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + validToken);
        when(jwtService.extraerEmail(validToken)).thenReturn(email);
        when(tokenBlacklistService.isBlacklisted(validToken)).thenReturn(false);
        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(jwtService.esTokenValido(validToken, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_ShouldSkipAuthForLoginEndpoint() throws Exception {
        // ✅ Configurar requestURI para login
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extraerEmail(anyString());
    }

    @Test
    void doFilterInternal_ShouldSkipAuthForRegisterEndpoint() throws Exception {
        // ✅ Configurar requestURI para register
        when(request.getRequestURI()).thenReturn("/api/auth/register");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extraerEmail(anyString());
    }

    @Test
    void shouldNotFilter_ShouldSkipSwaggerEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/swagger-ui/index.html");
        boolean shouldNotFilter = jwtAuthFilter.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();

        when(request.getRequestURI()).thenReturn("/v3/api-docs");
        shouldNotFilter = jwtAuthFilter.shouldNotFilter(request);
        assertThat(shouldNotFilter).isTrue();
    }

    @Test
    void shouldNotFilter_ShouldNotSkipOtherEndpoints() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/ventas");
        boolean shouldNotFilter = jwtAuthFilter.shouldNotFilter(request);
        assertThat(shouldNotFilter).isFalse();
    }
}
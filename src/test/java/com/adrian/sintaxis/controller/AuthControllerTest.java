package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.AuthResponseDTO;
import com.adrian.sintaxis.dto.LoginRequestDTO;
import com.adrian.sintaxis.dto.RegisterRequestDTO;
import com.adrian.sintaxis.exception.EmailYaExistenteException;
import com.adrian.sintaxis.exception.RolInvalidoException;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import com.adrian.sintaxis.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.adrian.sintaxis.exception.AuthExceptionHandler;
import com.adrian.sintaxis.exception.ClienteExceptionHandler;
import com.adrian.sintaxis.exception.ProductoExceptionHandler;
import com.adrian.sintaxis.exception.VentaExceptionHandler;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({ProductoExceptionHandler.class, AuthExceptionHandler.class, ClienteExceptionHandler.class, VentaExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDTO loginRequest;
    private RegisterRequestDTO registerRequest;
    private AuthResponseDTO authResponse;

    @BeforeEach
    void setUp() {
        // Login Request
        loginRequest = new LoginRequestDTO();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password123");

        // Register Request - COMPLETO
        registerRequest = new RegisterRequestDTO();
        registerRequest.setNombre("Test User");
        registerRequest.setApellido("Apellido Test");
        registerRequest.setEmail("test@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setRol("CLIENTE");
        registerRequest.setTelefono("123456789");
        registerRequest.setDireccion("Calle Falsa 123");

        // Auth Response
        authResponse = new AuthResponseDTO();
        authResponse.setToken("jwt-token-123");
        authResponse.setEmail("test@test.com");
        authResponse.setRol("CLIENTE");
    }

    // ==================== TESTS DE LOGIN ====================

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValid() throws Exception {
        when(authService.login(any(LoginRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void login_ShouldReturnBadRequest_WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequestDTO.class)))
                .thenThrow(new BadCredentialsException("Credenciales inválidas"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());  // 🔥 CAMBIADO: 401 en lugar de 400
    }

    // ==================== TESTS DE REGISTRO ====================

    @Test
    void register_ShouldReturnToken_WhenUserIsCreated() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())  // 🔥 CAMBIADO: 201 en lugar de 200
                .andExpect(jsonPath("$.token").value("jwt-token-123"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenEmailAlreadyExists() throws Exception {
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new EmailYaExistenteException("El email ya está registrado"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());  // 🔥 CAMBIADO: 409 en lugar de 400
    }

    // ==================== TESTS DE VALIDACIÓN DE ROL ====================

    @Test
    void register_ShouldReturnBadRequest_WhenRolIsInvalid() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        invalidRequest.setNombre("Test User");
        invalidRequest.setApellido("Apellido Test");
        invalidRequest.setEmail("test@test.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setRol("INVALIDO");  // Rol no válido

        // 🔥 SI EL CONTROLADOR NO VALIDA EL ROL, EL SERVICE DEBE LANZAR EXCEPCIÓN
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RolInvalidoException("Rol inválido: INVALIDO"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "email-invalido"})
    void register_ShouldReturnBadRequest_WhenFieldIsEmptyOrInvalid(String invalidValue) throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        invalidRequest.setNombre(invalidValue);
        invalidRequest.setApellido(invalidValue);
        invalidRequest.setEmail(invalidValue);
        invalidRequest.setPassword("password123");
        invalidRequest.setRol("CLIENTE");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }


    @Test
    void register_ShouldReturnBadRequest_WhenRolIsNull() throws Exception {
        RegisterRequestDTO invalidRequest = new RegisterRequestDTO();
        invalidRequest.setNombre("Test User");
        invalidRequest.setApellido("Apellido Test");
        invalidRequest.setEmail("test@test.com");
        invalidRequest.setPassword("password123");
        invalidRequest.setRol(null);  // Rol null

        // 🔥 SI EL CONTROLADOR NO VALIDA EL ROL, EL SERVICE DEBE LANZAR EXCEPCIÓN
        when(authService.register(any(RegisterRequestDTO.class)))
                .thenThrow(new RolInvalidoException("El rol es obligatorio"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}
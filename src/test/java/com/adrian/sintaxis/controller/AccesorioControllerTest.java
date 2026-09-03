package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.AccesorioRequestDTO;
import com.adrian.sintaxis.dto.AccesorioResponseDTO;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import com.adrian.sintaxis.service.IAccesorioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.adrian.sintaxis.exception.AuthExceptionHandler;
import com.adrian.sintaxis.exception.ClienteExceptionHandler;
import com.adrian.sintaxis.exception.ProductoExceptionHandler;
import com.adrian.sintaxis.exception.VentaExceptionHandler;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccesorioController.class)
@Import({ProductoExceptionHandler.class, AuthExceptionHandler.class, ClienteExceptionHandler.class, VentaExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class AccesorioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IAccesorioService accesorioService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private AccesorioRequestDTO accesorioRequestDTO;
    private AccesorioResponseDTO accesorioResponseDTO;

    @BeforeEach
    void setUp() {
        accesorioRequestDTO = new AccesorioRequestDTO();
        accesorioRequestDTO.setNombre("Cable USB-C");
        accesorioRequestDTO.setMarca("Belkin");
        accesorioRequestDTO.setTipoAccesorio("Cable");
        accesorioRequestDTO.setPrecio(29.99);
        accesorioRequestDTO.setStock(50);
        accesorioRequestDTO.setStockMinimo(10);
        accesorioRequestDTO.setEsOriginal(true);
        accesorioRequestDTO.setMaterial("Plástico");
        accesorioRequestDTO.setColor("Blanco");
        accesorioRequestDTO.setMarcasCompatibles(Arrays.asList("Apple", "Samsung", "Xiaomi"));

        accesorioResponseDTO = new AccesorioResponseDTO();
        accesorioResponseDTO.setIdProducto(1L);
        accesorioResponseDTO.setNombre("Cable USB-C");
        accesorioResponseDTO.setMarca("Belkin");
        accesorioResponseDTO.setTipoAccesorio("Cable");
        accesorioResponseDTO.setPrecio(29.99);
        accesorioResponseDTO.setStock(50);
        accesorioResponseDTO.setStockMinimo(10);
        accesorioResponseDTO.setActivo(true);
        accesorioResponseDTO.setFechaAlta(LocalDateTime.now());
        accesorioResponseDTO.setEsOriginal(true);
        accesorioResponseDTO.setMaterial("Plástico");
        accesorioResponseDTO.setColor("Blanco");
        accesorioResponseDTO.setMarcasCompatibles(Arrays.asList("Apple", "Samsung", "Xiaomi"));
    }

    @Test
    void listarTodos_ShouldReturnPageOfAccesorios() throws Exception {
        Page<AccesorioResponseDTO> page = new PageImpl<>(Arrays.asList(accesorioResponseDTO));
        when(accesorioService.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/accesorios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void buscarPorId_ShouldReturnAccesorio_WhenExists() throws Exception {
        when(accesorioService.buscarPorId(1L)).thenReturn(Optional.of(accesorioResponseDTO));

        mockMvc.perform(get("/api/accesorios/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1L));
    }

    @Test
    void guardar_ShouldReturnCreatedAccesorio() throws Exception {
        when(accesorioService.guardar(any(AccesorioRequestDTO.class), eq(null)))
                .thenReturn(accesorioResponseDTO);

        mockMvc.perform(post("/api/accesorios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(accesorioRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1L));
    }

    @Test
    void buscarPorTipo_ShouldReturnAccesorios() throws Exception {
        List<AccesorioResponseDTO> accesorios = Arrays.asList(accesorioResponseDTO);
        when(accesorioService.buscarPorTipo("Cable")).thenReturn(accesorios);

        mockMvc.perform(get("/api/accesorios/tipo")
                        .param("tipo", "Cable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarOriginales_ShouldReturnOnlyOriginalAccesorios() throws Exception {
        List<AccesorioResponseDTO> accesorios = Arrays.asList(accesorioResponseDTO);
        when(accesorioService.buscarOriginales()).thenReturn(accesorios);

        mockMvc.perform(get("/api/accesorios/originales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
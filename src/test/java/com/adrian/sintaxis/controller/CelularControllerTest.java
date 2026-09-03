package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.CelularRequestDTO;
import com.adrian.sintaxis.dto.CelularResponseDTO;
import com.adrian.sintaxis.exception.ProductoNoEncontradoException;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import com.adrian.sintaxis.service.ICelularService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CelularController.class)
@Import({ProductoExceptionHandler.class, AuthExceptionHandler.class, ClienteExceptionHandler.class, VentaExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class CelularControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ICelularService celularService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private CelularRequestDTO celularRequestDTO;
    private CelularResponseDTO celularResponseDTO;
    private CelularResponseDTO celularResponseDTO2;

    @BeforeEach
    void setUp() {
        // 🔥 DTO para enviar en el request (SIN ID)
        celularRequestDTO = new CelularRequestDTO();
        celularRequestDTO.setNombre("iPhone 15 Pro");
        celularRequestDTO.setMarca("Apple");
        celularRequestDTO.setModelo("iPhone 15 Pro");
        celularRequestDTO.setDescripcion("iPhone 15 Pro 256GB");
        celularRequestDTO.setPrecio(1199.99);
        celularRequestDTO.setStock(15);
        celularRequestDTO.setStockMinimo(5);
        celularRequestDTO.setImagenUrl("https://example.com/iphone15pro.jpg");
        celularRequestDTO.setColor("Negro");
        celularRequestDTO.setProcesador("A17 Pro");
        celularRequestDTO.setAlmacenamientoGB(256);
        celularRequestDTO.setRamGB(8);
        celularRequestDTO.setPantallaPulgadas(6.1);
        celularRequestDTO.setBateriaMAh(3274);
        celularRequestDTO.setSistemaOperativo("iOS 17");
        celularRequestDTO.setEsLibre(true);

        // 🔥 Response DTO 1 (CON ID)
        celularResponseDTO = new CelularResponseDTO();
        celularResponseDTO.setIdProducto(1L);
        celularResponseDTO.setNombre("iPhone 15 Pro");
        celularResponseDTO.setMarca("Apple");
        celularResponseDTO.setModelo("iPhone 15 Pro");
        celularResponseDTO.setDescripcion("iPhone 15 Pro 256GB");
        celularResponseDTO.setPrecio(1199.99);
        celularResponseDTO.setStock(15);
        celularResponseDTO.setStockMinimo(5);
        celularResponseDTO.setActivo(true);
        celularResponseDTO.setFechaAlta(LocalDateTime.now());
        celularResponseDTO.setImagenUrl("https://example.com/iphone15pro.jpg");
        celularResponseDTO.setColor("Negro");
        celularResponseDTO.setProcesador("A17 Pro");
        celularResponseDTO.setAlmacenamientoGB(256);
        celularResponseDTO.setRamGB(8);
        celularResponseDTO.setPantallaPulgadas(6.1);
        celularResponseDTO.setBateriaMAh(3274);
        celularResponseDTO.setSistemaOperativo("iOS 17");
        celularResponseDTO.setEsLibre(true);

        // 🔥 Response DTO 2
        celularResponseDTO2 = new CelularResponseDTO();
        celularResponseDTO2.setIdProducto(2L);
        celularResponseDTO2.setNombre("Samsung Galaxy S24");
        celularResponseDTO2.setMarca("Samsung");
        celularResponseDTO2.setModelo("Galaxy S24");
        celularResponseDTO2.setDescripcion("Galaxy S24 256GB");
        celularResponseDTO2.setPrecio(999.99);
        celularResponseDTO2.setStock(20);
        celularResponseDTO2.setStockMinimo(5);
        celularResponseDTO2.setActivo(true);
        celularResponseDTO2.setFechaAlta(LocalDateTime.now());
        celularResponseDTO2.setImagenUrl("https://example.com/galaxys24.jpg");
        celularResponseDTO2.setColor("Violeta");
        celularResponseDTO2.setProcesador("Exynos 2400");
        celularResponseDTO2.setAlmacenamientoGB(256);
        celularResponseDTO2.setRamGB(8);
        celularResponseDTO2.setPantallaPulgadas(6.2);
        celularResponseDTO2.setBateriaMAh(4000);
        celularResponseDTO2.setSistemaOperativo("Android 14");
        celularResponseDTO2.setEsLibre(true);
    }

    // ==================== TESTS GET ALL ====================

    @Test
    void listarTodos_ShouldReturnPageOfCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO, celularResponseDTO2);
        Page<CelularResponseDTO> page = new PageImpl<>(celulares);

        when(celularService.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/celulares")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].nombre").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.content[1].nombre").value("Samsung Galaxy S24"));
    }

    // ==================== TESTS GET BY ID ====================

    @Test
    void buscarPorId_ShouldReturnCelular_WhenExists() throws Exception {
        when(celularService.buscarPorId(1L)).thenReturn(Optional.of(celularResponseDTO));

        mockMvc.perform(get("/api/celulares/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1L))
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"));
    }

    @Test
    void buscarPorId_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(celularService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/celulares/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS CREATE ====================

    @Test
    void guardar_ShouldReturnCreatedCelular_WhenValid() throws Exception {
        when(celularService.guardar(any(CelularRequestDTO.class), eq(null))).thenReturn(celularResponseDTO);

        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(celularRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idProducto").value(1L))
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"));
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenNombreIsEmpty() throws Exception {
        CelularRequestDTO invalidDTO = new CelularRequestDTO();
        invalidDTO.setNombre("");
        invalidDTO.setMarca("Apple");
        invalidDTO.setModelo("iPhone 15 Pro");
        invalidDTO.setPrecio(1199.99);
        invalidDTO.setStock(15);
        invalidDTO.setProcesador("A17 Pro");
        invalidDTO.setAlmacenamientoGB(256);
        invalidDTO.setRamGB(8);
        invalidDTO.setPantallaPulgadas(6.1);
        invalidDTO.setBateriaMAh(3274);
        invalidDTO.setSistemaOperativo("iOS 17");

        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenPrecioIsNegative() throws Exception {
        CelularRequestDTO invalidDTO = new CelularRequestDTO();
        invalidDTO.setNombre("iPhone 15 Pro");
        invalidDTO.setMarca("Apple");
        invalidDTO.setModelo("iPhone 15 Pro");
        invalidDTO.setPrecio(-10.0);
        invalidDTO.setStock(15);
        invalidDTO.setProcesador("A17 Pro");
        invalidDTO.setAlmacenamientoGB(256);
        invalidDTO.setRamGB(8);
        invalidDTO.setPantallaPulgadas(6.1);
        invalidDTO.setBateriaMAh(3274);
        invalidDTO.setSistemaOperativo("iOS 17");

        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS UPDATE ====================

    @Test
    void actualizar_ShouldReturnUpdatedCelular_WhenValid() throws Exception {
        when(celularService.actualizar(eq(1L), any(CelularRequestDTO.class))).thenReturn(celularResponseDTO);

        mockMvc.perform(put("/api/celulares/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(celularRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProducto").value(1L))
                .andExpect(jsonPath("$.nombre").value("iPhone 15 Pro"));
    }

    @Test
    void actualizar_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(celularService.actualizar(eq(99L), any(CelularRequestDTO.class)))
                .thenThrow(new ProductoNoEncontradoException("Celular no encontrado"));

        mockMvc.perform(put("/api/celulares/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(celularRequestDTO)))
                .andExpect(status().isNotFound());
    }
    // ==================== TESTS DELETE ====================

    @Test
    void eliminar_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(celularService).eliminar(1L);

        mockMvc.perform(delete("/api/celulares/1"))
                .andExpect(status().isNoContent());
    }

    // ==================== TESTS FILTERS ====================

    @Test
    void buscarConFiltros_ShouldReturnFilteredCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO);
        Page<CelularResponseDTO> page = new PageImpl<>(celulares);

        when(celularService.buscarConFiltros(
                eq("Apple"), eq("iOS 17"), eq(1000.0), eq(1500.0), eq(true), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/celulares/buscar")
                        .param("marca", "Apple")
                        .param("sistemaOperativo", "iOS 17")
                        .param("precioMin", "1000")
                        .param("precioMax", "1500")
                        .param("esLibre", "true")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nombre").value("iPhone 15 Pro"));
    }

    @Test
    void buscarPorModelo_ShouldReturnCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO);
        when(celularService.buscarPorModelo("iPhone")).thenReturn(celulares);

        mockMvc.perform(get("/api/celulares/modelo")
                        .param("modelo", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("iPhone 15 Pro"));
    }

    @Test
    void buscarPorSistemaOperativo_ShouldReturnCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO, celularResponseDTO2);
        when(celularService.buscarPorSistemaOperativo("Android")).thenReturn(celulares);

        mockMvc.perform(get("/api/celulares/sistema-operativo")
                        .param("sistemaOperativo", "Android"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void buscarLibres_ShouldReturnOnlyFreeCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO, celularResponseDTO2);
        when(celularService.buscarLibres()).thenReturn(celulares);

        mockMvc.perform(get("/api/celulares/libres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void reponerStock_ShouldReturnUpdatedCelular() throws Exception {
        CelularResponseDTO updatedCelular = new CelularResponseDTO();
        updatedCelular.setIdProducto(1L);
        updatedCelular.setNombre("iPhone 15 Pro");
        updatedCelular.setStock(20);  // Stock aumentado

        when(celularService.reponerStock(1L, 5)).thenReturn(updatedCelular);

        mockMvc.perform(patch("/api/celulares/1/stock")
                        .param("cantidad", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(20));
    }

    @Test
    void stockBajo_ShouldReturnCelularesWithLowStock() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO);
        when(celularService.stockBajo()).thenReturn(celulares);

        mockMvc.perform(get("/api/celulares/stock-bajo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void buscarPorRangoAlmacenamiento_ShouldReturnCelulares() throws Exception {
        List<CelularResponseDTO> celulares = Arrays.asList(celularResponseDTO, celularResponseDTO2);
        when(celularService.buscarPorRangoAlmacenamiento(128, 512)).thenReturn(celulares);

        mockMvc.perform(get("/api/celulares/almacenamiento")
                        .param("min", "128")
                        .param("max", "512"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}

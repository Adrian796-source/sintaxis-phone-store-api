package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.ClienteRequestDTO;
import com.adrian.sintaxis.dto.ClienteResponseDTO;
import com.adrian.sintaxis.dto.ConfiguracionPuntosDTO;
import com.adrian.sintaxis.dto.PerfilConHistorialDTO;
import com.adrian.sintaxis.exception.GlobalExceptionHandler;
import com.adrian.sintaxis.exception.PuntosInvalidosException;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import com.adrian.sintaxis.service.IClienteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClienteController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IClienteService clienteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private ClienteRequestDTO clienteRequestDTO;
    private ClienteResponseDTO clienteResponseDTO;
    private ClienteResponseDTO clienteResponseDTO2;
    private ConfiguracionPuntosDTO configuracionPuntosDTO;

    @BeforeEach
    void setUp() {
        // ... (igual que antes)
        clienteRequestDTO = new ClienteRequestDTO();
        clienteRequestDTO.setNombre("Juan");
        clienteRequestDTO.setApellido("Perez");
        clienteRequestDTO.setEmail("juan@test.com");
        clienteRequestDTO.setTelefono("123456789");
        clienteRequestDTO.setDireccion("Calle Falsa 123");

        clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setIdCliente(1L);
        clienteResponseDTO.setNombre("Juan");
        clienteResponseDTO.setApellido("Perez");
        clienteResponseDTO.setEmail("juan@test.com");
        clienteResponseDTO.setTelefono("123456789");
        clienteResponseDTO.setDireccion("Calle Falsa 123");
        clienteResponseDTO.setPuntosAcumulados(50);
        clienteResponseDTO.setEsVip(false);
        clienteResponseDTO.setActivo(true);
        clienteResponseDTO.setFechaRegistro(LocalDateTime.now());

        clienteResponseDTO2 = new ClienteResponseDTO();
        clienteResponseDTO2.setIdCliente(2L);
        clienteResponseDTO2.setNombre("Maria");
        clienteResponseDTO2.setApellido("Gomez");
        clienteResponseDTO2.setEmail("maria@test.com");
        clienteResponseDTO2.setTelefono("987654321");
        clienteResponseDTO2.setDireccion("Calle Real 456");
        clienteResponseDTO2.setPuntosAcumulados(150);
        clienteResponseDTO2.setEsVip(true);
        clienteResponseDTO2.setActivo(true);
        clienteResponseDTO2.setFechaRegistro(LocalDateTime.now());

        configuracionPuntosDTO = new ConfiguracionPuntosDTO();
        configuracionPuntosDTO.setPuntosParaVip(100);
        configuracionPuntosDTO.setPesosPorPunto(10.0);
    }

    // ==================== TESTS GET ALL ====================

    @Test
    void listarTodos_ShouldReturnListOfClientes() throws Exception {
        List<ClienteResponseDTO> clientes = Arrays.asList(clienteResponseDTO, clienteResponseDTO2);
        when(clienteService.listarTodos()).thenReturn(clientes);

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nombre").value("Juan"))
                .andExpect(jsonPath("$[1].nombre").value("Maria"));
    }

    @Test
    void listarTodos_ShouldReturnEmptyList_WhenNoClientes() throws Exception {
        when(clienteService.listarTodos()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== TESTS GET BY ID ====================

    @Test
    void buscarPorId_ShouldReturnCliente_WhenExists() throws Exception {
        when(clienteService.buscarPorId(1L)).thenReturn(Optional.of(clienteResponseDTO));

        mockMvc.perform(get("/api/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void buscarPorId_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con id: 99"));

        mockMvc.perform(get("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS CREATE ====================

    @Test
    void guardar_ShouldReturnCreatedCliente_WhenValid() throws Exception {
        when(clienteService.guardar(any(ClienteRequestDTO.class))).thenReturn(clienteResponseDTO);

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenNombreIsEmpty() throws Exception {
        ClienteRequestDTO invalidDTO = new ClienteRequestDTO();
        invalidDTO.setNombre("");
        invalidDTO.setApellido("Perez");
        invalidDTO.setEmail("juan@test.com");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        ClienteRequestDTO invalidDTO = new ClienteRequestDTO();
        invalidDTO.setNombre("Juan");
        invalidDTO.setApellido("Perez");
        invalidDTO.setEmail("email-invalido");

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS UPDATE ====================

    @Test
    void actualizar_ShouldReturnUpdatedCliente_WhenValid() throws Exception {
        when(clienteService.actualizar(eq(1L), any(ClienteRequestDTO.class))).thenReturn(clienteResponseDTO);

        mockMvc.perform(put("/api/clientes/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.nombre").value("Juan"));
    }

    @Test
    void actualizar_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(clienteService.actualizar(eq(99L), any(ClienteRequestDTO.class)))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        mockMvc.perform(put("/api/clientes/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(clienteRequestDTO)))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS DELETE ====================

    @Test
    void eliminar_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(clienteService).eliminar(1L);

        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        doThrow(new ResourceNotFoundException("Cliente no encontrado")).when(clienteService).eliminar(99L);

        mockMvc.perform(delete("/api/clientes/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS SEARCH ====================

    @Test
    void buscarPorNombre_ShouldReturnClientes() throws Exception {
        List<ClienteResponseDTO> clientes = Arrays.asList(clienteResponseDTO);
        when(clienteService.buscarPorNombre("Juan")).thenReturn(clientes);

        mockMvc.perform(get("/api/clientes/buscar")
                        .param("nombre", "Juan"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Juan"));
    }

    @Test
    void buscarPorNombre_ShouldReturnEmpty_WhenNoMatch() throws Exception {
        when(clienteService.buscarPorNombre("NoExiste")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/clientes/buscar")
                        .param("nombre", "NoExiste"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscarPorEmail_ShouldReturnCliente_WhenExists() throws Exception {
        when(clienteService.buscarPorEmail("juan@test.com")).thenReturn(Optional.of(clienteResponseDTO));

        mockMvc.perform(get("/api/clientes/email")
                        .param("email", "juan@test.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }

    @Test
    void buscarPorEmail_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(clienteService.buscarPorEmail("noexiste@test.com"))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado con email: noexiste@test.com"));

        mockMvc.perform(get("/api/clientes/email")
                        .param("email", "noexiste@test.com"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS VIP ====================

    @Test
    void listarVip_ShouldReturnOnlyVipClientes() throws Exception {
        List<ClienteResponseDTO> vipClientes = Arrays.asList(clienteResponseDTO2);
        when(clienteService.listarVip()).thenReturn(vipClientes);

        mockMvc.perform(get("/api/clientes/vip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].esVip").value(true))
                .andExpect(jsonPath("$[0].nombre").value("Maria"));
    }

    @Test
    void listarVip_ShouldReturnEmpty_WhenNoVipClientes() throws Exception {
        when(clienteService.listarVip()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/clientes/vip"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ==================== TESTS PUNTOS ====================

    @Test
    void agregarPuntos_ShouldReturnUpdatedCliente() throws Exception {
        ClienteResponseDTO updatedCliente = new ClienteResponseDTO();
        updatedCliente.setIdCliente(1L);
        updatedCliente.setNombre("Juan");
        updatedCliente.setPuntosAcumulados(60);

        when(clienteService.agregarPuntos(1L, 10)).thenReturn(updatedCliente);

        mockMvc.perform(patch("/api/clientes/1/puntos")
                        .param("puntos", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosAcumulados").value(60));
    }

    @Test
    void agregarPuntos_ShouldReturnBadRequest_WhenPuntosIsNegative() throws Exception {
        when(clienteService.agregarPuntos(eq(1L), eq(-5)))
                .thenThrow(new PuntosInvalidosException("Los puntos no pueden ser negativos"));

        mockMvc.perform(patch("/api/clientes/1/puntos")
                        .param("puntos", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void agregarPuntos_ShouldReturnNotFound_WhenClienteDoesNotExist() throws Exception {
        when(clienteService.agregarPuntos(eq(99L), eq(10)))
                .thenThrow(new ResourceNotFoundException("Cliente no encontrado"));

        mockMvc.perform(patch("/api/clientes/99/puntos")
                        .param("puntos", "10"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS CONFIGURACION PUNTOS ====================

    @Test
    void obtenerConfiguracion_ShouldReturnConfiguracion() throws Exception {
        when(clienteService.obtenerConfiguracion()).thenReturn(configuracionPuntosDTO);

        mockMvc.perform(get("/api/clientes/configuracion-puntos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosParaVip").value(100))
                .andExpect(jsonPath("$.pesosPorPunto").value(10.0));
    }

    @Test
    void actualizarConfiguracion_ShouldReturnUpdatedConfiguracion() throws Exception {
        ConfiguracionPuntosDTO updatedConfig = new ConfiguracionPuntosDTO();
        updatedConfig.setPuntosParaVip(150);
        updatedConfig.setPesosPorPunto(15.0);

        when(clienteService.actualizarConfiguracion(any(ConfiguracionPuntosDTO.class)))
                .thenReturn(updatedConfig);

        mockMvc.perform(put("/api/clientes/configuracion-puntos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(configuracionPuntosDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.puntosParaVip").value(150))
                .andExpect(jsonPath("$.pesosPorPunto").value(15.0));
    }

    // ==================== TESTS MI PERFIL ====================

    @Test
    @WithMockUser(username = "juan@test.com", roles = "CLIENTE")  // 🔥 CLAVE: Mockea el usuario autenticado
    void obtenerMiPerfil_ShouldReturnPerfilConHistorial() throws Exception {
        PerfilConHistorialDTO perfilDTO = new PerfilConHistorialDTO();
        perfilDTO.setIdCliente(1L);
        perfilDTO.setNombre("Juan");
        perfilDTO.setApellido("Perez");
        perfilDTO.setEmail("juan@test.com");
        perfilDTO.setPuntosAcumulados(50);
        perfilDTO.setEsVip(false);

        when(clienteService.obtenerPerfilConHistorial("juan@test.com")).thenReturn(perfilDTO);

        mockMvc.perform(get("/api/clientes/mi-perfil"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCliente").value(1L))
                .andExpect(jsonPath("$.email").value("juan@test.com"));
    }
}
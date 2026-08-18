package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.DetalleVentaRequestDTO;
import com.adrian.sintaxis.dto.DetalleVentaResponseDTO;
import com.adrian.sintaxis.dto.ReporteVentaDTO;
import com.adrian.sintaxis.dto.VentaRequestDTO;
import com.adrian.sintaxis.dto.VentaResponseDTO;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import com.adrian.sintaxis.service.IVentaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VentaController.class)
@AutoConfigureMockMvc(addFilters = false)
class VentaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IVentaService ventaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @Autowired
    private ObjectMapper objectMapper;

    private VentaRequestDTO ventaRequestDTO;
    private VentaResponseDTO ventaResponseDTO;
    private VentaResponseDTO ventaResponseDTO2;

    @BeforeEach
    void setUp() {
        // 🔥 DTO para enviar en el request
        ventaRequestDTO = new VentaRequestDTO();
        ventaRequestDTO.setIdCliente(1L);
        ventaRequestDTO.setEstado("PENDIENTE");
        ventaRequestDTO.setMetodoPago("TARJETA_CREDITO");
        ventaRequestDTO.setDescuento(10.0);

        // 🔥 Detalles de la venta - SOLO idProducto y cantidad (sin precioUnitario)
        DetalleVentaRequestDTO detalle1 = new DetalleVentaRequestDTO();
        detalle1.setIdProducto(1L);
        detalle1.setCantidad(2);

        DetalleVentaRequestDTO detalle2 = new DetalleVentaRequestDTO();
        detalle2.setIdProducto(2L);
        detalle2.setCantidad(1);

        ventaRequestDTO.setDetalles(Arrays.asList(detalle1, detalle2));

        // 🔥 Response DTO 1
        ventaResponseDTO = new VentaResponseDTO();
        ventaResponseDTO.setIdVenta(1L);
        ventaResponseDTO.setIdCliente(1L);
        ventaResponseDTO.setNombreCliente("Juan");
        ventaResponseDTO.setApellidoCliente("Perez");
        ventaResponseDTO.setFecha(LocalDateTime.now());
        ventaResponseDTO.setSubtotal(2429.97);
        ventaResponseDTO.setDescuento(10.0);
        ventaResponseDTO.setTotal(2419.97);
        ventaResponseDTO.setEstado("CONFIRMADO");
        ventaResponseDTO.setMetodoPago("TARJETA_CREDITO");

        // 🔥 Detalles de respuesta
        DetalleVentaResponseDTO detalleResp1 = new DetalleVentaResponseDTO();
        detalleResp1.setIdDetalleVenta(1L);
        detalleResp1.setIdProducto(1L);
        detalleResp1.setNombreProducto("iPhone 15 Pro");
        detalleResp1.setCantidad(2);
        detalleResp1.setPrecioUnitario(1199.99);
        detalleResp1.setSubtotalDetalle(2399.98);

        DetalleVentaResponseDTO detalleResp2 = new DetalleVentaResponseDTO();
        detalleResp2.setIdDetalleVenta(2L);
        detalleResp2.setIdProducto(2L);
        detalleResp2.setNombreProducto("Cable USB-C");
        detalleResp2.setCantidad(1);
        detalleResp2.setPrecioUnitario(29.99);
        detalleResp2.setSubtotalDetalle(29.99);

        ventaResponseDTO.setDetalles(Arrays.asList(detalleResp1, detalleResp2));

        // 🔥 Response DTO 2
        ventaResponseDTO2 = new VentaResponseDTO();
        ventaResponseDTO2.setIdVenta(2L);
        ventaResponseDTO2.setIdCliente(2L);
        ventaResponseDTO2.setNombreCliente("Maria");
        ventaResponseDTO2.setApellidoCliente("Lopez");
        ventaResponseDTO2.setFecha(LocalDateTime.now().minusDays(1));
        ventaResponseDTO2.setSubtotal(999.99);
        ventaResponseDTO2.setDescuento(0.0);
        ventaResponseDTO2.setTotal(999.99);
        ventaResponseDTO2.setEstado("PENDIENTE");
        ventaResponseDTO2.setMetodoPago("EFECTIVO");
    }

    // ==================== TESTS CREATE ====================

    @Test
    void guardar_ShouldReturnCreatedVenta_WhenValid() throws Exception {
        when(ventaService.guardar(any(VentaRequestDTO.class))).thenReturn(ventaResponseDTO);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idVenta").value(1L))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(2419.97));
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenClienteIdIsNull() throws Exception {
        VentaRequestDTO invalidDTO = new VentaRequestDTO();
        invalidDTO.setIdCliente(null);
        invalidDTO.setEstado("PENDIENTE");
        invalidDTO.setMetodoPago("TARJETA_CREDITO");
        invalidDTO.setDescuento(0.0);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenEstadoIsNull() throws Exception {
        VentaRequestDTO invalidDTO = new VentaRequestDTO();
        invalidDTO.setIdCliente(1L);
        invalidDTO.setEstado(null);
        invalidDTO.setMetodoPago("TARJETA_CREDITO");
        invalidDTO.setDescuento(0.0);

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void guardar_ShouldReturnBadRequest_WhenDetallesIsEmpty() throws Exception {
        VentaRequestDTO invalidDTO = new VentaRequestDTO();
        invalidDTO.setIdCliente(1L);
        invalidDTO.setEstado("PENDIENTE");
        invalidDTO.setMetodoPago("TARJETA_CREDITO");
        invalidDTO.setDescuento(0.0);
        invalidDTO.setDetalles(Arrays.asList());

        mockMvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS GET BY ID ====================

    @Test
    void buscarPorId_ShouldReturnVenta_WhenExists() throws Exception {
        when(ventaService.buscarPorId(1L)).thenReturn(Optional.of(ventaResponseDTO));

        mockMvc.perform(get("/api/ventas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(1L))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.total").value(2419.97));
    }

    @Test
    void buscarPorId_ShouldReturnNotFound_WhenDoesNotExist() throws Exception {
        when(ventaService.buscarPorId(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/ventas/99"))
                .andExpect(status().isNotFound());
    }

    // ==================== TESTS GET ALL ====================

    @Test
    void listarTodos_ShouldReturnPageOfVentas() throws Exception {
        List<VentaResponseDTO> ventas = Arrays.asList(ventaResponseDTO, ventaResponseDTO2);
        Page<VentaResponseDTO> page = new PageImpl<>(ventas);

        when(ventaService.listarTodos(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/ventas")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].idVenta").value(1L))
                .andExpect(jsonPath("$.content[1].idVenta").value(2L));
    }

    @Test
    void listarTodos_ShouldReturnEmptyPage_WhenNoVentas() throws Exception {
        Page<VentaResponseDTO> emptyPage = new PageImpl<>(Arrays.asList());

        when(ventaService.listarTodos(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/ventas")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // ==================== TESTS UPDATE ====================

    @Test
    void actualizar_ShouldReturnUpdatedVenta_WhenValid() throws Exception {
        when(ventaService.actualizar(eq(1L), any(VentaRequestDTO.class))).thenReturn(ventaResponseDTO);

        mockMvc.perform(put("/api/ventas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaRequestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(1L))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));
    }

    @Test
    void actualizar_ShouldReturnBadRequest_WhenDoesNotExist() throws Exception {
        when(ventaService.actualizar(eq(99L), any(VentaRequestDTO.class)))
                .thenThrow(new RuntimeException("Venta no encontrada"));

        mockMvc.perform(put("/api/ventas/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ventaRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS DELETE ====================

    @Test
    void eliminar_ShouldReturnNoContent_WhenExists() throws Exception {
        doNothing().when(ventaService).eliminar(1L);

        mockMvc.perform(delete("/api/ventas/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void eliminar_ShouldReturnBadRequest_WhenDoesNotExist() throws Exception {
        doThrow(new RuntimeException("Venta no encontrada")).when(ventaService).eliminar(99L);

        mockMvc.perform(delete("/api/ventas/99"))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS FILTERS ====================

    @Test
    void buscarPorCliente_ShouldReturnVentas() throws Exception {
        List<VentaResponseDTO> ventas = Arrays.asList(ventaResponseDTO);
        when(ventaService.buscarPorCliente(1L)).thenReturn(ventas);

        mockMvc.perform(get("/api/ventas/cliente/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idVenta").value(1L));
    }

    @Test
    void buscarPorCliente_ShouldReturnEmpty_WhenNoVentas() throws Exception {
        when(ventaService.buscarPorCliente(99L)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/ventas/cliente/99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscarPorEstado_ShouldReturnVentas() throws Exception {
        List<VentaResponseDTO> ventas = Arrays.asList(ventaResponseDTO);
        when(ventaService.buscarPorEstado("CONFIRMADO")).thenReturn(ventas);

        mockMvc.perform(get("/api/ventas/estado")
                        .param("estado", "CONFIRMADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("CONFIRMADO"));
    }

    @Test
    void buscarPorEstado_ShouldReturnEmpty_WhenNoVentas() throws Exception {
        when(ventaService.buscarPorEstado("CANCELADO")).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/ventas/estado")
                        .param("estado", "CANCELADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void buscarPorRangoFechas_ShouldReturnVentas() throws Exception {
        List<VentaResponseDTO> ventas = Arrays.asList(ventaResponseDTO, ventaResponseDTO2);
        LocalDateTime desde = LocalDateTime.now().minusDays(7);
        LocalDateTime hasta = LocalDateTime.now();

        when(ventaService.buscarPorRangoFechas(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(ventas);

        mockMvc.perform(get("/api/ventas/fechas")
                        .param("desde", desde.toString())
                        .param("hasta", hasta.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    // ==================== TESTS CAMBIAR ESTADO ====================

    @Test
    void cambiarEstado_ShouldReturnUpdatedVenta() throws Exception {
        VentaResponseDTO updatedVenta = new VentaResponseDTO();
        updatedVenta.setIdVenta(1L);
        updatedVenta.setEstado("ENTREGADO");
        updatedVenta.setTotal(2419.97);

        when(ventaService.cambiarEstado(1L, "ENTREGADO")).thenReturn(updatedVenta);

        mockMvc.perform(patch("/api/ventas/1/estado")
                        .param("estado", "ENTREGADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idVenta").value(1L))
                .andExpect(jsonPath("$.estado").value("ENTREGADO"));
    }

    @Test
    void cambiarEstado_ShouldReturnBadRequest_WhenEstadoIsInvalid() throws Exception {
        // 🔥 FIX: Simular que el servicio lanza IllegalArgumentException para estado inválido
        when(ventaService.cambiarEstado(eq(1L), eq("INVALIDO")))
                .thenThrow(new IllegalArgumentException("Estado inválido: INVALIDO"));

        mockMvc.perform(patch("/api/ventas/1/estado")
                        .param("estado", "INVALIDO"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cambiarEstado_ShouldReturnBadRequest_WhenVentaDoesNotExist() throws Exception {
        when(ventaService.cambiarEstado(99L, "CONFIRMADO"))
                .thenThrow(new RuntimeException("Venta no encontrada"));

        mockMvc.perform(patch("/api/ventas/99/estado")
                        .param("estado", "CONFIRMADO"))
                .andExpect(status().isBadRequest());
    }

    // ==================== TESTS REPORTES ====================

    @Test
    void totalRecaudado_ShouldReturnTotal() throws Exception {
        LocalDateTime desde = LocalDateTime.now().minusDays(30);
        LocalDateTime hasta = LocalDateTime.now();
        Double total = 5000.0;

        when(ventaService.totalRecaudado(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(total);

        mockMvc.perform(get("/api/ventas/reportes/total-recaudado")
                        .param("desde", desde.toString())
                        .param("hasta", hasta.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecaudado").value(5000.0));
    }

    @Test
    void ventasPorEstado_ShouldReturnMap() throws Exception {
        Map<String, Long> reporte = Map.of(
                "PENDIENTE", 5L,
                "CONFIRMADO", 10L,
                "ENTREGADO", 8L,
                "CANCELADO", 2L
        );

        when(ventaService.ventasPorEstado()).thenReturn(reporte);

        mockMvc.perform(get("/api/ventas/reportes/por-estado"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.PENDIENTE").value(5))
                .andExpect(jsonPath("$.CONFIRMADO").value(10))
                .andExpect(jsonPath("$.ENTREGADO").value(8))
                .andExpect(jsonPath("$.CANCELADO").value(2));
    }

    @Test
    void topClientes_ShouldReturnList() throws Exception {
        // 🔥 Usando el constructor de ReporteVentaDTO (etiqueta, valor)
        ReporteVentaDTO reporte1 = new ReporteVentaDTO("Juan Perez", 5000.0);
        ReporteVentaDTO reporte2 = new ReporteVentaDTO("Maria Lopez", 3000.0);

        List<ReporteVentaDTO> topClientes = Arrays.asList(reporte1, reporte2);

        when(ventaService.topClientes()).thenReturn(topClientes);

        mockMvc.perform(get("/api/ventas/reportes/top-clientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].etiqueta").value("Juan Perez"))
                .andExpect(jsonPath("$[0].valor").value(5000.0))
                .andExpect(jsonPath("$[1].etiqueta").value("Maria Lopez"))
                .andExpect(jsonPath("$[1].valor").value(3000.0));
    }

    // ==================== TESTS MIS VENTAS ====================

    @Test
    @WithMockUser(username = "juan@test.com")  // 🔥 FIX: Usar @WithMockUser en lugar de .principal()
    void misVentas_ShouldReturnVentasDelUsuarioAutenticado() throws Exception {
        List<VentaResponseDTO> ventas = Arrays.asList(ventaResponseDTO);
        String email = "juan@test.com";

        when(ventaService.misVentas(email)).thenReturn(ventas);

        mockMvc.perform(get("/api/ventas/mis-ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].idVenta").value(1L));
    }

    @Test
    @WithMockUser(username = "sinventas@test.com")  // 🔥 FIX: Usar @WithMockUser en lugar de .principal()
    void misVentas_ShouldReturnEmpty_WhenNoVentas() throws Exception {
        String email = "sinventas@test.com";

        when(ventaService.misVentas(email)).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/ventas/mis-ventas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
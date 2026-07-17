package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.ReporteVentaDTO;
import com.adrian.sintaxis.dto.VentaRequestDTO;
import com.adrian.sintaxis.dto.VentaResponseDTO;
import com.adrian.sintaxis.service.IVentaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@Tag(name = "Ventas", description = "Gestión de ventas y reportes. Requiere rol ADMIN o EMPLEADO, excepto mis-ventas que es para CLIENTE.")
public class VentaController {

    private final IVentaService ventaService;

    @Operation(summary = "Crear venta", description = "Registra una nueva venta. Reduce el stock de los productos automáticamente. Si el estado es Pagada, suma 10 puntos al cliente.")
    @PostMapping
    public ResponseEntity<VentaResponseDTO> guardar(@Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ventaService.guardar(dto));
    }

    @Operation(summary = "Buscar venta por ID", description = "Devuelve los datos completos de una venta incluyendo sus detalles.")
    @GetMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ventaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Listar todas las ventas", description = "Devuelve todas las ventas paginadas, ordenadas por fecha.")
    @GetMapping
    public ResponseEntity<Page<VentaResponseDTO>> listarTodos(
            @PageableDefault(size = 10, sort = "fecha") Pageable pageable) {
        return ResponseEntity.ok(ventaService.listarTodos(pageable));
    }

    @Operation(summary = "Actualizar venta", description = "Modifica el método de pago y descuento de una venta. No se puede modificar una venta cancelada.")
    @PutMapping("/{id}")
    public ResponseEntity<VentaResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody VentaRequestDTO dto) {
        return ResponseEntity.ok(ventaService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar venta", description = "Borrado lógico: marca la venta como inactiva sin eliminarla de la base de datos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ventaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Mis ventas", description = "Devuelve las ventas del cliente autenticado. Solo disponible para usuarios con rol CLIENTE.")
    @GetMapping("/mis-ventas")
    public ResponseEntity<List<VentaResponseDTO>> misVentas(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ventaService.misVentas(userDetails.getUsername()));
    }

    @Operation(summary = "Ventas por cliente", description = "Devuelve todas las ventas de un cliente específico por su ID.")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<VentaResponseDTO>> buscarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(ventaService.buscarPorCliente(idCliente));
    }

    @Operation(summary = "Ventas por estado", description = "Filtra ventas por estado. Estados válidos: Pendiente, Pagada, Entregada, Cancelada.")
    @GetMapping("/estado")
    public ResponseEntity<List<VentaResponseDTO>> buscarPorEstado(@RequestParam String estado) {
        return ResponseEntity.ok(ventaService.buscarPorEstado(estado));
    }

    @Operation(summary = "Ventas por rango de fechas", description = "Devuelve las ventas entre dos fechas. Formato: 2026-01-01T00:00:00")
    @GetMapping("/fechas")
    public ResponseEntity<List<VentaResponseDTO>> buscarPorRangoFechas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(ventaService.buscarPorRangoFechas(desde, hasta));
    }

    @Operation(summary = "Cambiar estado de venta", description = "Actualiza el estado de una venta. Al cancelar: devuelve el stock y resta los puntos al cliente si estaba Pagada. Al pasar a Pagada: suma puntos según configuración.")
    @PatchMapping("/{id}/estado")
    public ResponseEntity<VentaResponseDTO> cambiarEstado(@PathVariable Long id, @RequestParam String estado) {
        return ResponseEntity.ok(ventaService.cambiarEstado(id, estado));
    }

    @Operation(summary = "Total recaudado por período", description = "Suma el total de todas las ventas con estado Pagada entre dos fechas. Formato: 2026-01-01T00:00:00")
    @GetMapping("/reportes/total-recaudado")
    public ResponseEntity<Map<String, Object>> totalRecaudado(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta) {
        return ResponseEntity.ok(Map.of("desde", desde, "hasta", hasta, "totalRecaudado", ventaService.totalRecaudado(desde, hasta)));
    }

    @Operation(summary = "Ventas agrupadas por estado", description = "Devuelve la cantidad de ventas en cada estado: Pendiente, Pagada, Entregada, Cancelada.")
    @GetMapping("/reportes/por-estado")
    public ResponseEntity<Map<String, Long>> ventasPorEstado() {
        return ResponseEntity.ok(ventaService.ventasPorEstado());
    }

    @Operation(summary = "Top clientes", description = "Devuelve los clientes ordenados por total gastado en ventas con estado Pagada.")
    @GetMapping("/reportes/top-clientes")
    public ResponseEntity<List<ReporteVentaDTO>> topClientes() {
        return ResponseEntity.ok(ventaService.topClientes());
    }
}

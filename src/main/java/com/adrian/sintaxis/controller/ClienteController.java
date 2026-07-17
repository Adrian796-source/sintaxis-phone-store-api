package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.ClienteRequestDTO;
import com.adrian.sintaxis.dto.ClienteResponseDTO;
import com.adrian.sintaxis.dto.ConfiguracionPuntosDTO;
import com.adrian.sintaxis.dto.PerfilConHistorialDTO;
import com.adrian.sintaxis.exception.ResourceNotFoundException;
import com.adrian.sintaxis.service.IClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Gestión de clientes. Todos los endpoints requieren rol ADMIN.")
public class ClienteController {

    private final IClienteService clienteService;

    @Operation(summary = "Crear cliente", description = "Registra un nuevo cliente en el sistema.")
    @PostMapping
    public ResponseEntity<ClienteResponseDTO> guardar(@Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.guardar(dto));
    }

    @Operation(summary = "Buscar cliente por ID", description = "Devuelve los datos completos de un cliente por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id)));
    }

    @Operation(summary = "Listar todos los clientes", description = "Devuelve la lista completa de clientes activos.")
    @GetMapping
    public ResponseEntity<List<ClienteResponseDTO>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @Operation(summary = "Actualizar cliente", description = "Modifica los datos de un cliente existente.")
    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponseDTO> actualizar(@PathVariable Long id, @Valid @RequestBody ClienteRequestDTO dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @Operation(summary = "Eliminar cliente", description = "Borrado lógico: marca el cliente como inactivo sin eliminarlo de la base de datos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar cliente por nombre", description = "Busca clientes cuyo nombre contenga el texto ingresado.")
    @GetMapping("/buscar")
    public ResponseEntity<List<ClienteResponseDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(clienteService.buscarPorNombre(nombre));
    }

    @Operation(summary = "Buscar cliente por email", description = "Devuelve el cliente que tenga el email exacto indicado.")
    @GetMapping("/email")
    public ResponseEntity<ClienteResponseDTO> buscarPorEmail(@RequestParam String email) {
        return ResponseEntity.ok(clienteService.buscarPorEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con email: " + email)));
    }

    @Operation(summary = "Listar clientes VIP", description = "Devuelve todos los clientes que alcanzaron el estado VIP por acumular 100 o más puntos.")
    @GetMapping("/vip")
    public ResponseEntity<List<ClienteResponseDTO>> listarVip() {
        return ResponseEntity.ok(clienteService.listarVip());
    }

    @Operation(summary = "Agregar puntos a cliente", description = "Suma puntos al cliente. Si llega a 100 puntos se promueve automáticamente a VIP.")
    @PatchMapping("/{id}/puntos")
    public ResponseEntity<ClienteResponseDTO> agregarPuntos(@PathVariable Long id, @RequestParam int puntos) {
        return ResponseEntity.ok(clienteService.agregarPuntos(id, puntos));
    }

    @Operation(summary = "Ver configuración de puntos", description = "Devuelve los parámetros actuales: puntos necesarios para VIP y pesos por punto.")
    @GetMapping("/configuracion-puntos")
    public ResponseEntity<ConfiguracionPuntosDTO> obtenerConfiguracion() {
        return ResponseEntity.ok(clienteService.obtenerConfiguracion());
    }

    @Operation(summary = "Actualizar configuración de puntos", description = "Modifica los parámetros del sistema de puntos. Solo ADMIN.")
    @PutMapping("/configuracion-puntos")
    public ResponseEntity<ConfiguracionPuntosDTO> actualizarConfiguracion(@Valid @RequestBody ConfiguracionPuntosDTO dto) {
        return ResponseEntity.ok(clienteService.actualizarConfiguracion(dto));
    }

    //  Obtener mi perfil con historial de compras (usando el token)
    @Operation(summary = "Obtener mi perfil completo con historial de compras")
    @GetMapping("/mi-perfil")
    public ResponseEntity<PerfilConHistorialDTO> obtenerMiPerfil(
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        PerfilConHistorialDTO perfil = clienteService.obtenerPerfilConHistorial(email);
        return ResponseEntity.ok(perfil);
    }

}

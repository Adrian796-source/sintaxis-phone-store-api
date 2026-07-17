package com.adrian.sintaxis.controller;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Registro, login, perfil y cambio de contraseña")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuario", description = "Crea un nuevo usuario. El rol puede ser ADMIN, EMPLEADO o CLIENTE. Si es CLIENTE se puede vincular con un idCliente existente.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(dto));
    }

    @Operation(summary = "Login", description = "Autentica al usuario y devuelve un token JWT para usar en los endpoints protegidos.")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Ver perfil", description = "Devuelve los datos del usuario autenticado. Si tiene un cliente vinculado también muestra sus datos, puntos y si es VIP.")
    @GetMapping("/perfil")
    public ResponseEntity<PerfilResponseDTO> perfil(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(authService.perfil(userDetails.getUsername()));
    }

    @Operation(summary = "Cambiar contraseña", description = "Permite al usuario autenticado cambiar su contraseña. Requiere ingresar la contraseña actual.")
    @PatchMapping("/cambiar-password")
    public ResponseEntity<Void> cambiarPassword(@AuthenticationPrincipal UserDetails userDetails,
                                                @Valid @RequestBody CambiarPasswordDTO dto) {
        authService.cambiarPassword(userDetails.getUsername(), dto);
        return ResponseEntity.noContent().build();
    }

    // ==================== ASOCIACIÓN CLIENTE ====================

    @Operation(summary = "Asociar cliente a usuario",
            description = "Asigna un cliente existente a un usuario. Requiere rol ADMIN.")
    @PatchMapping("/usuarios/{idUsuario}/asociar-cliente")
    public ResponseEntity<String> asociarCliente(
            @PathVariable Long idUsuario,
            @RequestBody AsociarClienteDTO dto) {

        authService.asociarCliente(idUsuario, dto.getIdCliente());
        return ResponseEntity.ok("Cliente asociado correctamente al usuario");
    }

    @Operation(summary = "Ver clientes sin usuario",
            description = "Lista todos los clientes que no tienen un usuario asociado.")
    @GetMapping("/clientes-sin-usuario")
    public ResponseEntity<List<ClienteResponseDTO>> clientesSinUsuario() {
        return ResponseEntity.ok(authService.clientesSinUsuario());
    }

    @PostMapping("/logout")
    @Operation(
            summary = "Cerrar sesión",
            description = "Invalida el token JWT actual agregándolo a la blacklist. El token no podrá ser utilizado nuevamente.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<LogoutResponseDTO> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        LogoutResponseDTO response = authService.logout(authHeader);
        return ResponseEntity.ok(response);
    }
}



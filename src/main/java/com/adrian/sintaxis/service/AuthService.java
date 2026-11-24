package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.Rol;
import com.adrian.sintaxis.model.Usuario;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.UsuarioRepository;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.adrian.sintaxis.exception.*;
import java.time.ZoneId;
import java.time.LocalDateTime;  // 🔴 IMPORT NUEVO
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    // Constante para el mensaje de error de usuario no encontrado
    private static final String USUARIO_NO_ENCONTRADO = "Usuario no encontrado";
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;


    public LogoutResponseDTO logout(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UsuarioNoEncontradoException("Token no válido");
        }

        String token = authHeader.substring(7);

        try {
            // Agregar token a la blacklist
            tokenBlacklistService.addToBlacklist(token);
            return new LogoutResponseDTO("Sesión cerrada exitosamente", true);
        } catch (Exception e) {
            return new LogoutResponseDTO("Error al cerrar sesión: " + e.getMessage(), false);
        }
    }

    @Transactional
    public AuthResponseDTO register(@Valid RegisterRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new EmailYaExistenteException("Ya existe un usuario con ese email");
        }

        // 2. VALIDAR QUE EL ROL SEA VÁLIDO (Acepta con o sin prefijo ROLE_)
        Rol rol;
        try {
            String rolStr = dto.getRol().toUpperCase();
            if (rolStr.startsWith("ROLE_")) {
                rolStr = rolStr.substring(5);
            }
            rol = Rol.valueOf(rolStr);
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException("Rol inválido. Los valores permitidos son: ADMIN, EMPLEADO, CLIENTE");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        //  Si NO viene idCliente, CREAR CLIENTE AUTOMÁTICAMENTE SÓLO SI EL ROL ES CLIENTE
        if (rol == Rol.CLIENTE) {
            if (dto.getIdCliente() != null) {
                Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                        .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con id: " + dto.getIdCliente()));
                usuario.setCliente(cliente);

                // Validar que el cliente no tenga ya un usuario asociado
                if (cliente.getUsuario() != null) {
                    throw new ClienteYaAsociadoException("El cliente ya está asociado a un usuario");
                }
            } else {
                // Crear cliente automático con los datos del usuario
                Cliente nuevoCliente = new Cliente();
                nuevoCliente.setNombre(dto.getNombre());
                nuevoCliente.setApellido(dto.getApellido() != null ? dto.getApellido() : "");
                nuevoCliente.setEmail(dto.getEmail());
                nuevoCliente.setTelefono(dto.getTelefono() != null ? dto.getTelefono() : "");
                nuevoCliente.setDireccion(dto.getDireccion() != null ? dto.getDireccion() : "");
                nuevoCliente.setFechaRegistro(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")));
                nuevoCliente.setEsVip(false);
                nuevoCliente.setPuntosAcumulados(0);
                nuevoCliente.setActivo(true);

                clienteRepository.save(nuevoCliente);
                usuario.setCliente(nuevoCliente);
            }
        }

        usuarioRepository.save(usuario);

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        String token = jwtService.generarToken(userDetails);

        //  Devolver respuesta con datos del cliente
        return buildAuthResponse(token, usuario);
    }

    public void cambiarPassword(String email, CambiarPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(USUARIO_NO_ENCONTRADO));

        if (!passwordEncoder.matches(dto.getPasswordActual(), usuario.getPassword())) {
            throw new PasswordIncorrectaException("La contraseña actual es incorrecta");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO dto) {
        // Por seguridad, si el usuario no existe no revelamos explícitamente que el correo no existe, 
        // pero aquí si existe generamos el token de recuperación (ej. válido por 15 minutos).
        usuarioRepository.findByEmail(dto.getEmail()).ifPresent(usuario -> {
            String codigoOtp = String.format("%06d", new java.util.Random().nextInt(1000000));
            usuario.setResetToken(codigoOtp);
            usuario.setResetTokenExpiry(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")).plusMinutes(10));
            usuarioRepository.save(usuario);

            // Log en consola para simular el envío del código OTP de 6 dígitos por email
            org.slf4j.LoggerFactory.getLogger(AuthService.class).warn(
                    "🔑 [RECUPERACIÓN DE CONTRASEÑA] Código OTP de 6 dígitos generado para {}: {} (Válido por 10 minutos)",
                    usuario.getEmail(), codigoOtp
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordDTO dto) {
        Usuario usuario = usuarioRepository.findByResetToken(dto.getToken())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Token de recuperación inválido o inexistente"));

        if (usuario.getResetTokenExpiry() == null || usuario.getResetTokenExpiry().isBefore(LocalDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires")))) {
            throw new IllegalArgumentException("El token de recuperación ha expirado. Solicite uno nuevo.");
        }

        usuario.setPassword(passwordEncoder.encode(dto.getPasswordNueva()));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }

    public PerfilResponseDTO perfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsuarioNoEncontradoException(USUARIO_NO_ENCONTRADO));

        PerfilResponseDTO dto = new PerfilResponseDTO();
        dto.setIdUsuario(usuario.getIdUsuario());
        dto.setNombre(usuario.getNombre());
        dto.setEmail(usuario.getEmail());
        dto.setRol(usuario.getRol().name());

        if (usuario.getCliente() != null) {
            dto.setIdCliente(usuario.getCliente().getIdCliente());
            dto.setNombreCliente(usuario.getCliente().getNombre());
            dto.setApellidoCliente(usuario.getCliente().getApellido());
            dto.setTelefono(usuario.getCliente().getTelefono());
            dto.setDireccion(usuario.getCliente().getDireccion());
            dto.setEsVip(usuario.getCliente().isEsVip());
            dto.setPuntosAcumulados(usuario.getCliente().getPuntosAcumulados());
        }

        return dto;
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword())
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(dto.getEmail());
        String token = jwtService.generarToken(userDetails);

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UsuarioNoEncontradoException(USUARIO_NO_ENCONTRADO));

        // Devolver respuesta con datos del cliente
        return buildAuthResponse(token, usuario);
    }

    // ==================== ASOCIACIÓN CLIENTE ====================

    @Transactional
    public void asociarCliente(Long idUsuario, Long idCliente) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + idUsuario));

        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new ClienteNoEncontradoException("Cliente no encontrado con id: " + idCliente));

        // Si el usuario ya tiene un cliente, lo reemplaza
        usuario.setCliente(cliente);
        usuarioRepository.save(usuario);
    }

    public List<ClienteResponseDTO> clientesSinUsuario() {
        List<Cliente> clientes = clienteRepository.findAll().stream()
                .filter(c -> c.getUsuario() == null)
                .toList();

        return clientes.stream()
                .map(this::toClienteResponseDTO)
                .toList();
    }

    public List<UsuarioResponseDTO> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponseDTO(
                        u.getIdUsuario(),
                        u.getNombre(),
                        u.getEmail(),
                        u.getRol() != null ? u.getRol().name() : "CLIENTE",
                        u.isActivo()
                ))
                .toList();
    }

    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
        Usuario u = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(USUARIO_NO_ENCONTRADO + " con id: " + id));
        return new UsuarioResponseDTO(
                u.getIdUsuario(),
                u.getNombre(),
                u.getEmail(),
                u.getRol() != null ? u.getRol().name() : "CLIENTE",
                u.isActivo()
        );
    }

    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Long id, UsuarioUpdateDTO dto) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(USUARIO_NO_ENCONTRADO + " con id: " + id));

        // Validar si el email cambió y si ya existe en otro usuario
        if (!usuario.getEmail().equalsIgnoreCase(dto.getEmail())) {
            if (usuarioRepository.existsByEmail(dto.getEmail())) {
                throw new EmailYaExistenteException("Ya existe otro usuario con ese email");
            }
            usuario.setEmail(dto.getEmail());
            // Si tiene cliente asociado, opcionalmente sincronizar su email si coincide
            if (usuario.getCliente() != null) {
                usuario.getCliente().setEmail(dto.getEmail());
                clienteRepository.save(usuario.getCliente());
            }
        }

        usuario.setNombre(dto.getNombre());
        usuario.setActivo(dto.isActivo());

        // Validar y actualizar rol
        Rol rol;
        try {
            String rolStr = dto.getRol().toUpperCase();
            if (rolStr.startsWith("ROLE_")) {
                rolStr = rolStr.substring(5);
            }
            rol = Rol.valueOf(rolStr);
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException("Rol inválido. Los valores permitidos son: ADMIN, EMPLEADO, CLIENTE");
        }
        usuario.setRol(rol);

        // Si se provee password nueva (no nula y no vacía), encriptarla y actualizarla
        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            if (dto.getPassword().length() < 6) {
                throw new IllegalArgumentException("La contraseña debe tener al menos 6 caracteres");
            }
            usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(usuario);

        return new UsuarioResponseDTO(
                usuario.getIdUsuario(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.isActivo()
        );
    }

    @Transactional
    public void eliminarUsuario(Long idUsuarioAEliminar, String emailAdminLogueado) {
        Usuario usuarioAEliminar = usuarioRepository.findById(idUsuarioAEliminar)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con id: " + idUsuarioAEliminar));

        if (usuarioAEliminar.getEmail().equalsIgnoreCase(emailAdminLogueado)) {
            throw new IllegalArgumentException("No puedes eliminar tu propia cuenta de administrador.");
        }

        // Si tiene un cliente asociado, rompemos la relación bidireccional limpiamente para evitar TransientObjectException / FK constraints
        if (usuarioAEliminar.getCliente() != null) {
            Cliente cliente = usuarioAEliminar.getCliente();
            cliente.setUsuario(null);
            usuarioAEliminar.setCliente(null);
            clienteRepository.save(cliente);
        }

        usuarioRepository.delete(usuarioAEliminar);
    }

    private ClienteResponseDTO toClienteResponseDTO(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setEmail(cliente.getEmail());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setFechaRegistro(cliente.getFechaRegistro());
        dto.setEsVip(cliente.isEsVip());
        dto.setPuntosAcumulados(cliente.getPuntosAcumulados());
        dto.setActivo(cliente.isActivo());
        return dto;
    }

    /**
     * Construye la respuesta de autenticación con los datos del cliente asociado
     */
    private AuthResponseDTO buildAuthResponse(String token, Usuario usuario) {
        AuthResponseDTO response = new AuthResponseDTO(token, usuario.getEmail(), usuario.getRol().name());

        if (usuario.getCliente() != null) {
            Cliente cliente = usuario.getCliente();
            response.setIdCliente(cliente.getIdCliente());
            response.setNombreCliente(cliente.getNombre());
            response.setApellidoCliente(cliente.getApellido());
            response.setTelefono(cliente.getTelefono());
            response.setDireccion(cliente.getDireccion());
            response.setEsVip(cliente.isEsVip());
            response.setPuntosAcumulados(cliente.getPuntosAcumulados());
        }

        return response;
    }
}
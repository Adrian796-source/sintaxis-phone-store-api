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
import java.util.stream.Collectors;

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

        // 2. VALIDAR QUE EL ROL SEA VÁLIDO
        Rol rol;
        try {
            rol = Rol.valueOf(dto.getRol().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException("Rol inválido. Los valores permitidos son: ADMIN, EMPLEADO, CLIENTE");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRol(rol);
        usuario.setActivo(true);

        //  Si NO viene idCliente, CREAR CLIENTE AUTOMÁTICAMENTE
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
                .collect(Collectors.toList());

        return clientes.stream()
                .map(this::toClienteResponseDTO)
                .collect(Collectors.toList());
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
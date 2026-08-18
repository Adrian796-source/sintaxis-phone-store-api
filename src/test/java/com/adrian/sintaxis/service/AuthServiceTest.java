package com.adrian.sintaxis.service;

import com.adrian.sintaxis.dto.*;
import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.Rol;
import com.adrian.sintaxis.model.Usuario;
import com.adrian.sintaxis.repository.ClienteRepository;
import com.adrian.sintaxis.repository.UsuarioRepository;
import com.adrian.sintaxis.security.JwtService;
import com.adrian.sintaxis.security.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private CambiarPasswordDTO cambiarPasswordDTO;
    private Usuario usuario;
    private Cliente cliente;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        // Configurar Cliente
        cliente = new Cliente();
        cliente.setIdCliente(1L);
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);

        // Configurar Usuario
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setPassword("encodedPassword");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
        usuario.setCliente(cliente);

        // Configurar RegisterRequestDTO
        registerRequestDTO = new RegisterRequestDTO();
        registerRequestDTO.setNombre("Juan");
        registerRequestDTO.setApellido("Perez");
        registerRequestDTO.setEmail("juan@test.com");
        registerRequestDTO.setPassword("password123");
        registerRequestDTO.setRol("CLIENTE");
        registerRequestDTO.setTelefono("123456789");
        registerRequestDTO.setDireccion("Calle Falsa 123");
        registerRequestDTO.setIdCliente(null);

        // Configurar LoginRequestDTO
        loginRequestDTO = new LoginRequestDTO();
        loginRequestDTO.setEmail("juan@test.com");
        loginRequestDTO.setPassword("password123");

        // Configurar CambiarPasswordDTO
        cambiarPasswordDTO = new CambiarPasswordDTO();
        cambiarPasswordDTO.setPasswordActual("oldPassword");
        cambiarPasswordDTO.setPasswordNueva("newPassword123");

        // Configurar UserDetails
        userDetails = org.springframework.security.core.userdetails.User
                .withUsername("juan@test.com")
                .password("password")
                .roles("USER")
                .build();
    }

    // ==================== TESTS LOGIN ====================

    @Test
    void login_ShouldReturnToken_WhenValid() {
        String mockToken = "mocked-jwt-token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("juan@test.com"))
                .thenReturn(userDetails);
        when(jwtService.generarToken(userDetails))
                .thenReturn(mockToken);
        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.of(usuario));

        AuthResponseDTO response = authService.login(loginRequestDTO);

        assertNotNull(response);
        assertEquals(mockToken, response.getToken());
        assertEquals("juan@test.com", response.getEmail());
        assertEquals("CLIENTE", response.getRol());
        assertEquals(1L, response.getIdCliente());

        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1))
                .generarToken(userDetails);
        verify(usuarioRepository, times(1))
                .findByEmail("juan@test.com");
    }

    @Test
    void login_ShouldThrowException_WhenUsuarioNotFound() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userDetailsService.loadUserByUsername("juan@test.com"))
                .thenReturn(userDetails);
        when(jwtService.generarToken(userDetails))
                .thenReturn("mock-token");
        when(usuarioRepository.findByEmail("juan@test.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ==================== TESTS REGISTER ====================

    @Test
    void register_ShouldCreateUserAndClient_WhenValid() {
        // ✅ Configurar el cliente sin usuario asociado
        cliente.setUsuario(null);

        // ✅ Mock de las validaciones
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        // ✅ Simular que clienteRepository.save() devuelve el cliente guardado con ID
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente clienteGuardado = invocation.getArgument(0);
            // ✅ Asignar un ID al cliente guardado
            clienteGuardado.setIdCliente(1L);
            // ✅ El cliente guardado NO tiene usuario aún (esto es importante)
            return clienteGuardado;
        });

        // ✅ Simular que usuarioRepository.save() devuelve el usuario guardado
        // ✅ Y que el usuario tiene el cliente asociado
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuarioGuardado = invocation.getArgument(0);
            usuarioGuardado.setIdUsuario(1L);
            // ✅ El usuario tiene el cliente que se creó anteriormente
            Cliente clienteUsuario = usuarioGuardado.getCliente();
            if (clienteUsuario != null) {
                clienteUsuario.setUsuario(usuarioGuardado); // Relación bidireccional
                clienteUsuario.setIdCliente(1L);
            }
            return usuarioGuardado;
        });

        // ✅ Mock de la generación del token
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtService.generarToken(userDetails)).thenReturn("jwt-token-mock");

        // ✅ Ejecutar
        AuthResponseDTO result = authService.register(registerRequestDTO);

        // ✅ Verificar
        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-mock");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getRol()).isEqualTo("CLIENTE");
        assertThat(result.getIdCliente()).isEqualTo(1L);

        verify(usuarioRepository).existsByEmail("juan@test.com");
        verify(clienteRepository).save(any(Cliente.class));
        verify(usuarioRepository).save(any(Usuario.class));
        verify(jwtService).generarToken(any(UserDetails.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailExists() {
        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Ya existe un usuario con ese email");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void register_ShouldThrowException_WhenRolInvalid() {
        registerRequestDTO.setRol("INVALIDO");

        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Rol inválido");

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(clienteRepository, never()).save(any(Cliente.class));
    }

    @Test
    void register_ShouldAssociateExistingClient_WhenIdClienteProvided() {
        registerRequestDTO.setIdCliente(1L);

        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);
        when(userDetailsService.loadUserByUsername("juan@test.com")).thenReturn(userDetails);
        when(jwtService.generarToken(userDetails)).thenReturn("jwt-token-mock");

        AuthResponseDTO result = authService.register(registerRequestDTO);

        assertThat(result).isNotNull();
        assertThat(result.getIdCliente()).isEqualTo(1L);

        verify(clienteRepository).findById(1L);
        verify(clienteRepository, never()).save(any(Cliente.class));
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void register_ShouldThrowException_WhenClienteNotFound() {
        registerRequestDTO.setIdCliente(99L);

        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void register_ShouldThrowException_WhenClienteHasUsuario() {
        registerRequestDTO.setIdCliente(1L);
        cliente.setUsuario(usuario);

        when(usuarioRepository.existsByEmail("juan@test.com")).thenReturn(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        assertThatThrownBy(() -> authService.register(registerRequestDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("El cliente ya está asociado a un usuario");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==================== TESTS LOGOUT ====================

    @Test
    void logout_ShouldReturnSuccess_WhenTokenValid() {
        String authHeader = "Bearer valid-token";

        // ✅ Para métodos void, usamos doNothing() o simplemente no mockeamos
        doNothing().when(tokenBlacklistService).addToBlacklist("valid-token");

        LogoutResponseDTO result = authService.logout(authHeader);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMensaje()).isEqualTo("Sesión cerrada exitosamente");

        verify(tokenBlacklistService).addToBlacklist("valid-token");
    }

    @Test
    void logout_ShouldThrowException_WhenAuthHeaderNull() {
        assertThatThrownBy(() -> authService.logout(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token no válido");

        verify(tokenBlacklistService, never()).addToBlacklist(anyString());
    }

    @Test
    void logout_ShouldThrowException_WhenAuthHeaderInvalid() {
        String authHeader = "InvalidHeader";
        assertThatThrownBy(() -> authService.logout(authHeader))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token no válido");

        verify(tokenBlacklistService, never()).addToBlacklist(anyString());
    }

    @Test
    void logout_ShouldReturnError_WhenTokenBlacklistFails() {
        String authHeader = "Bearer invalid-token";
        doThrow(new RuntimeException("Blacklist error")).when(tokenBlacklistService).addToBlacklist("invalid-token");

        LogoutResponseDTO result = authService.logout(authHeader);

        assertThat(result).isNotNull();
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getMensaje()).contains("Error al cerrar sesión");

        verify(tokenBlacklistService).addToBlacklist("invalid-token");
    }

    // ==================== TESTS CAMBIAR PASSWORD ====================

    @Test
    void cambiarPassword_ShouldUpdatePassword_WhenValid() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        authService.cambiarPassword("juan@test.com", cambiarPasswordDTO);

        verify(usuarioRepository).findByEmail("juan@test.com");
        verify(passwordEncoder).matches("oldPassword", "encodedPassword");
        verify(passwordEncoder).encode("newPassword123");
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void cambiarPassword_ShouldThrowException_WhenUsuarioNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.cambiarPassword("noexiste@test.com", cambiarPasswordDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void cambiarPassword_ShouldThrowException_WhenPasswordIncorrect() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(false);

        assertThatThrownBy(() -> authService.cambiarPassword("juan@test.com", cambiarPasswordDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("La contraseña actual es incorrecta");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==================== TESTS PERFIL ====================

    @Test
    void perfil_ShouldReturnProfile_WhenUsuarioExists() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        PerfilResponseDTO result = authService.perfil("juan@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(1L);
        assertThat(result.getNombre()).isEqualTo("Juan");
        assertThat(result.getEmail()).isEqualTo("juan@test.com");
        assertThat(result.getRol()).isEqualTo("CLIENTE");
        assertThat(result.getIdCliente()).isEqualTo(1L);
        assertThat(result.getNombreCliente()).isEqualTo("Juan");
        assertThat(result.getApellidoCliente()).isEqualTo("Perez");

        verify(usuarioRepository).findByEmail("juan@test.com");
    }

    @Test
    void perfil_ShouldReturnProfileWithoutClienteData_WhenUsuarioHasNoCliente() {
        usuario.setCliente(null);
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        PerfilResponseDTO result = authService.perfil("juan@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getIdUsuario()).isEqualTo(1L);
        assertThat(result.getIdCliente()).isNull();
        assertThat(result.getNombreCliente()).isNull();

        verify(usuarioRepository).findByEmail("juan@test.com");
    }

    @Test
    void perfil_ShouldThrowException_WhenUsuarioNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.perfil("noexiste@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado");
    }

    // ==================== TESTS ASOCIAR CLIENTE ====================

    @Test
    void asociarCliente_ShouldAssociateClient_WhenValid() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        authService.asociarCliente(1L, 1L);

        assertThat(usuario.getCliente()).isEqualTo(cliente);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void asociarCliente_ShouldThrowException_WhenUsuarioNotFound() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.asociarCliente(99L, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Usuario no encontrado con id: 99");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    void asociarCliente_ShouldThrowException_WhenClienteNotFound() {
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.asociarCliente(1L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cliente no encontrado con id: 99");

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    // ==================== TESTS CLIENTES SIN USUARIO ====================
    @Test
    void clientesSinUsuario_ShouldReturnClientsWithoutUser() {
        // ✅ El primer cliente TIENE usuario asociado
        cliente.setUsuario(usuario);  // ← Importante: este cliente NO debe aparecer en el resultado

        // ✅ El segundo cliente NO tiene usuario asociado
        Cliente cliente2 = new Cliente();
        cliente2.setIdCliente(2L);
        cliente2.setNombre("Maria");
        cliente2.setApellido("Lopez");
        cliente2.setEmail("maria@test.com");
        cliente2.setUsuario(null);  // ← Este SÍ debe aparecer

        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente, cliente2));

        List<ClienteResponseDTO> result = authService.clientesSinUsuario();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getIdCliente()).isEqualTo(2L);
        assertThat(result.get(0).getNombre()).isEqualTo("Maria");

        verify(clienteRepository).findAll();
    }

    @Test
    void clientesSinUsuario_ShouldReturnEmpty_WhenAllClientsHaveUser() {
        cliente.setUsuario(usuario);
        when(clienteRepository.findAll()).thenReturn(Arrays.asList(cliente));

        List<ClienteResponseDTO> result = authService.clientesSinUsuario();

        assertThat(result).isEmpty();
        verify(clienteRepository).findAll();
    }

    @Test
    void clientesSinUsuario_ShouldReturnEmpty_WhenNoClients() {
        when(clienteRepository.findAll()).thenReturn(List.of());

        List<ClienteResponseDTO> result = authService.clientesSinUsuario();

        assertThat(result).isEmpty();
        verify(clienteRepository).findAll();
    }
}
package com.adrian.sintaxis.security;

import com.adrian.sintaxis.model.Rol;
import com.adrian.sintaxis.model.Usuario;
import com.adrian.sintaxis.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setIdUsuario(1L);
        usuario.setNombre("Juan");
        usuario.setEmail("juan@test.com");
        usuario.setPassword("encodedPassword");
        usuario.setRol(Rol.CLIENTE);
        usuario.setActivo(true);
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        when(usuarioRepository.findByEmail("juan@test.com")).thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("juan@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("juan@test.com");
        assertThat(result.getPassword()).isEqualTo("encodedPassword");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_CLIENTE");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserIsAdmin() {
        usuario.setRol(Rol.ADMIN);
        when(usuarioRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserIsEmpleado() {
        usuario.setRol(Rol.EMPLEADO);
        when(usuarioRepository.findByEmail("empleado@test.com")).thenReturn(Optional.of(usuario));

        UserDetails result = userDetailsService.loadUserByUsername("empleado@test.com");

        assertThat(result).isNotNull();
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_EMPLEADO");
    }

    @Test
    void loadUserByUsername_ShouldThrowException_WhenUserNotFound() {
        when(usuarioRepository.findByEmail("noexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("noexiste@test.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuario no encontrado: noexiste@test.com");
    }
}
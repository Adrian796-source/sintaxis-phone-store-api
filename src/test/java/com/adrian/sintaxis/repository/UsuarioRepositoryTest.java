package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.Rol;
import com.adrian.sintaxis.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    private Usuario usuarioTest;

    @BeforeEach
    void setUp() {
        // Crear cliente de prueba
        Cliente cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido("Pérez");
        cliente.setEmail("juan@example.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);

        // Crear usuario de prueba
        usuarioTest = new Usuario();
        usuarioTest.setNombre("Juan");
        usuarioTest.setEmail("juan@example.com");
        usuarioTest.setPassword("encodedPassword123");
        usuarioTest.setRol(Rol.CLIENTE);
        usuarioTest.setActivo(true);
        usuarioTest.setCliente(cliente);
        usuarioTest = usuarioRepository.save(usuarioTest);
    }

    @Test
    void shouldFindByEmail() {
        // Cuando
        Optional<Usuario> found = usuarioRepository.findByEmail("juan@example.com");

        // Entonces
        assertTrue(found.isPresent());
        assertEquals("juan@example.com", found.get().getEmail());
        assertEquals("Juan", found.get().getNombre());
        assertEquals(Rol.CLIENTE, found.get().getRol());
    }

    @Test
    void shouldReturnEmptyWhenEmailNotFound() {
        // Cuando
        Optional<Usuario> found = usuarioRepository.findByEmail("noexiste@example.com");

        // Entonces
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldCheckIfEmailExists() {
        // Cuando
        boolean exists = usuarioRepository.existsByEmail("juan@example.com");
        boolean notExists = usuarioRepository.existsByEmail("noexiste@example.com");

        // Entonces
        assertTrue(exists);
        assertFalse(notExists);
    }

    @Test
    void shouldFindById() {
        // Cuando
        Optional<Usuario> found = usuarioRepository.findById(usuarioTest.getIdUsuario());

        // Entonces
        assertTrue(found.isPresent());
        assertEquals(usuarioTest.getIdUsuario(), found.get().getIdUsuario());
        assertEquals("juan@example.com", found.get().getEmail());
    }

    @Test
    void shouldSaveUsuario() {
        // Dado
        Cliente cliente = new Cliente();
        cliente.setNombre("Maria");
        cliente.setApellido("Gomez");
        cliente.setEmail("maria@example.com");
        cliente.setTelefono("987654321");
        cliente.setDireccion("Avenida Siempre Viva 742");
        cliente.setActivo(true);
        cliente = clienteRepository.save(cliente);

        Usuario nuevo = new Usuario();
        nuevo.setNombre("Maria");
        nuevo.setEmail("maria@example.com");
        nuevo.setPassword("encodedPassword456");
        nuevo.setRol(Rol.ADMIN);
        nuevo.setActivo(true);
        nuevo.setCliente(cliente);

        // Cuando
        Usuario saved = usuarioRepository.save(nuevo);

        // Entonces
        assertNotNull(saved.getIdUsuario());
        assertEquals("maria@example.com", saved.getEmail());
        assertEquals(Rol.ADMIN, saved.getRol());
        assertNotNull(saved.getCliente());
        assertEquals("Maria", saved.getCliente().getNombre());
    }

    @Test
    void shouldDeleteUsuario() {
        // Dado
        Long id = usuarioTest.getIdUsuario();

        // Cuando
        usuarioRepository.deleteById(id);
        Optional<Usuario> found = usuarioRepository.findById(id);

        // Entonces
        assertTrue(found.isEmpty());
    }

    @Test
    void shouldUpdateUsuario() {
        // Dado
        usuarioTest.setNombre("Juan Carlos");
        usuarioTest.setRol(Rol.EMPLEADO);

        // Cuando
        Usuario updated = usuarioRepository.save(usuarioTest);
        Optional<Usuario> found = usuarioRepository.findById(updated.getIdUsuario());

        // Entonces
        assertTrue(found.isPresent());
        assertEquals("Juan Carlos", found.get().getNombre());
        assertEquals(Rol.EMPLEADO, found.get().getRol());
    }
}
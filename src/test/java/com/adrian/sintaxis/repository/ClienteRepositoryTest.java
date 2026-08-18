package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Cliente;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ClienteRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cliente cliente1;
    private Cliente cliente2;
    private Cliente cliente3;

    @BeforeEach
    void setUp() {
        // Cliente 1 - Normal
        cliente1 = new Cliente();
        cliente1.setNombre("Juan");
        cliente1.setApellido("Perez");
        cliente1.setEmail("juan@test.com");
        cliente1.setTelefono("123456789");
        cliente1.setDireccion("Calle Falsa 123");
        cliente1.setFechaRegistro(LocalDateTime.now());
        cliente1.setEsVip(false);
        cliente1.setPuntosAcumulados(0);
        cliente1.setActivo(true);
        entityManager.persist(cliente1);

        // Cliente 2 - VIP
        cliente2 = new Cliente();
        cliente2.setNombre("Maria");
        cliente2.setApellido("Lopez");
        cliente2.setEmail("maria@test.com");
        cliente2.setTelefono("987654321");
        cliente2.setDireccion("Avenida Siempre Viva 742");
        cliente2.setFechaRegistro(LocalDateTime.now().minusDays(10));
        cliente2.setEsVip(true);
        cliente2.setPuntosAcumulados(150);
        cliente2.setActivo(true);
        entityManager.persist(cliente2);

        // Cliente 3 - Inactivo
        cliente3 = new Cliente();
        cliente3.setNombre("Carlos");
        cliente3.setApellido("Garcia");
        cliente3.setEmail("carlos@test.com");
        cliente3.setTelefono("555555555");
        cliente3.setDireccion("Calle Principal 456");
        cliente3.setFechaRegistro(LocalDateTime.now().minusDays(5));
        cliente3.setEsVip(false);
        cliente3.setPuntosAcumulados(0);
        cliente3.setActivo(false);
        entityManager.persist(cliente3);

        entityManager.flush();
    }

    @Test
    void findByNombreContainingIgnoreCase_ShouldReturnClientes() {
        List<Cliente> result = clienteRepository.findByNombreContainingIgnoreCase("juan");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Juan");
    }

    @Test
    void findByNombreContainingIgnoreCase_ShouldReturnMultipleClientes() {
        Cliente cliente4 = new Cliente();
        cliente4.setNombre("Juana");
        cliente4.setApellido("Martinez");
        cliente4.setEmail("juana@test.com");
        cliente4.setActivo(true);
        entityManager.persist(cliente4);
        entityManager.flush();

        List<Cliente> result = clienteRepository.findByNombreContainingIgnoreCase("jua");

        assertThat(result).hasSize(2);
        assertThat(result).extracting("nombre").containsExactlyInAnyOrder("Juan", "Juana");
    }

    @Test
    void findByNombreContainingIgnoreCase_ShouldReturnEmpty_WhenNoMatch() {
        List<Cliente> result = clienteRepository.findByNombreContainingIgnoreCase("Inexistente");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEmail_ShouldReturnCliente_WhenExists() {
        Optional<Cliente> result = clienteRepository.findByEmail("juan@test.com");

        assertThat(result).isPresent();
        assertThat(result.get().getNombre()).isEqualTo("Juan");
        assertThat(result.get().getEmail()).isEqualTo("juan@test.com");
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenNotFound() {
        Optional<Cliente> result = clienteRepository.findByEmail("noexiste@test.com");

        assertThat(result).isEmpty();
    }

    @Test
    void findByEsVipTrue_ShouldReturnOnlyVipClientes() {
        List<Cliente> result = clienteRepository.findByEsVipTrue();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNombre()).isEqualTo("Maria");
        assertThat(result.get(0).isEsVip()).isTrue();
    }

    @Test
    void findByEsVipTrue_ShouldReturnEmpty_WhenNoVipClientes() {
        cliente2.setEsVip(false);
        entityManager.persist(cliente2);
        entityManager.flush();

        List<Cliente> result = clienteRepository.findByEsVipTrue();

        assertThat(result).isEmpty();
    }
}
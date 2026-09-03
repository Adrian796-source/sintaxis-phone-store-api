package com.adrian.sintaxis.repository;

import com.adrian.sintaxis.model.Cliente;
import com.adrian.sintaxis.model.Venta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class VentaRepositoryTest {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Cliente cliente;
    private Venta venta1;
    private Venta venta2;
    private Venta venta3;

    @BeforeEach
    void setUp() {
        // Crear cliente
        cliente = new Cliente();
        cliente.setNombre("Juan");
        cliente.setApellido("Perez");
        cliente.setEmail("juan@test.com");
        cliente.setTelefono("123456789");
        cliente.setDireccion("Calle Falsa 123");
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setEsVip(false);
        cliente.setPuntosAcumulados(0);
        cliente.setActivo(true);
        entityManager.persist(cliente);

        // Crear venta 1 - Pagada
        venta1 = new Venta();
        venta1.setCliente(cliente);
        venta1.setFecha(LocalDateTime.now());
        venta1.setEstado("Pagada");
        venta1.setMetodoPago("TARJETA_CREDITO");
        venta1.setSubtotal(1000.0);
        venta1.setDescuento(0.0);
        venta1.setTotal(1000.0);
        venta1.setActivo(true);
        entityManager.persist(venta1);

        // Crear venta 2 - Pendiente
        venta2 = new Venta();
        venta2.setCliente(cliente);
        venta2.setFecha(LocalDateTime.now().minusDays(1));
        venta2.setEstado("Pendiente");
        venta2.setMetodoPago("EFECTIVO");
        venta2.setSubtotal(500.0);
        venta2.setDescuento(10.0);
        venta2.setTotal(490.0);
        venta2.setActivo(true);
        entityManager.persist(venta2);

        // Crear venta 3 - Pagada (otro día)
        venta3 = new Venta();
        venta3.setCliente(cliente);
        venta3.setFecha(LocalDateTime.now().minusDays(5));
        venta3.setEstado("Pagada");
        venta3.setMetodoPago("TRANSFERENCIA");
        venta3.setSubtotal(2000.0);
        venta3.setDescuento(100.0);
        venta3.setTotal(1900.0);
        venta3.setActivo(true);
        entityManager.persist(venta3);

        entityManager.flush();
    }

    @Test
    void findByClienteIdCliente_ShouldReturnVentas() {
        List<Venta> result = ventaRepository.findByClienteIdCliente(cliente.getIdCliente());
        assertThat(result)
                .hasSize(3)
                .allMatch(v -> v.getCliente().getIdCliente().equals(cliente.getIdCliente()));
    }

    @Test
    void findByClienteIdCliente_ShouldReturnEmpty_WhenNoVentas() {
        List<Venta> result = ventaRepository.findByClienteIdCliente(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findByEstado_ShouldReturnVentasByEstado() {
        List<Venta> result = ventaRepository.findByEstado("Pagada");
        assertThat(result)
                .hasSize(2)
                .allMatch(v -> v.getEstado().equals("Pagada"));
    }

    @Test
    void findByEstado_ShouldReturnEmpty_WhenEstadoNotFound() {
        List<Venta> result = ventaRepository.findByEstado("Cancelada");

        assertThat(result).isEmpty();
    }

    @Test
    void findByFechaBetween_ShouldReturnVentasInRange() {
        LocalDateTime desde = LocalDateTime.now().minusDays(2);
        LocalDateTime hasta = LocalDateTime.now().plusDays(1);

        List<Venta> result = ventaRepository.findByFechaBetween(desde, hasta);

        assertThat(result).hasSize(2); // venta1 y venta2
        assertThat(result).extracting("idVenta").containsExactlyInAnyOrder(venta1.getIdVenta(), venta2.getIdVenta());
    }

    @Test
    void findByFechaBetween_ShouldReturnEmpty_WhenNoVentasInRange() {
        LocalDateTime desde = LocalDateTime.now().minusDays(10);
        LocalDateTime hasta = LocalDateTime.now().minusDays(8);

        List<Venta> result = ventaRepository.findByFechaBetween(desde, hasta);

        assertThat(result).isEmpty();
    }

    @Test
    void totalRecaudadoEntreFechas_ShouldReturnSum() {
        LocalDateTime desde = LocalDateTime.now().minusDays(6);
        LocalDateTime hasta = LocalDateTime.now().plusDays(1);

        Double total = ventaRepository.totalRecaudadoEntreFechas(desde, hasta);

        // Solo ventas con estado "Pagada": 1000 + 1900 = 2900
        assertThat(total).isEqualTo(2900.0);
    }

    @Test
    void totalRecaudadoEntreFechas_ShouldReturnZero_WhenNoVentas() {
        LocalDateTime desde = LocalDateTime.now().minusDays(10);
        LocalDateTime hasta = LocalDateTime.now().minusDays(8);

        Double total = ventaRepository.totalRecaudadoEntreFechas(desde, hasta);

        assertThat(total).isEqualTo(0.0);
    }

    @Test
    void contarPorEstado_ShouldReturnCountByEstado() {
        List<Object[]> result = ventaRepository.contarPorEstado();

        assertThat(result).hasSize(2);

        // Verificar que hay 2 "Pagada" y 1 "Pendiente"
        for (Object[] row : result) {
            String estado = (String) row[0];
            Long count = (Long) row[1];
            if (estado.equals("Pagada")) {
                assertThat(count).isEqualTo(2);
            } else if (estado.equals("Pendiente")) {
                assertThat(count).isEqualTo(1);
            }
        }
    }

    @Test
    void topClientes_ShouldReturnTopClientesByTotal() {
        List<Object[]> result = ventaRepository.topClientes();

        assertThat(result)
                .isNotEmpty()
                .hasSize(1);

        Object[] row = result.get(0);
        Long idCliente = (Long) row[0];
        String nombre = (String) row[1];
        String apellido = (String) row[2];
        Long cantidadVentas = (Long) row[3];
        Double totalGastado = (Double) row[4];

        assertThat(idCliente).isEqualTo(cliente.getIdCliente());
        assertThat(nombre).isEqualTo("Juan");
        assertThat(apellido).isEqualTo("Perez");
        assertThat(cantidadVentas).isEqualTo(2); // Solo ventas "Pagada"
        assertThat(totalGastado).isEqualTo(2900.0);
    }
}
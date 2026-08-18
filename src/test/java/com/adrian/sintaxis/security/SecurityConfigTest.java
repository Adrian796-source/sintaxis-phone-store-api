package com.adrian.sintaxis.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldAllowAccessToLoginEndpoint() throws Exception {
        // ✅ Primero registrar un usuario
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"LoginTest\",\"email\":\"login@test.com\",\"password\":\"password123\",\"rol\":\"CLIENTE\",\"apellido\":\"Test\"}"))
                .andExpect(status().isCreated());

        // ✅ Luego hacer login con las credenciales correctas
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"login@test.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowAccessToRegisterEndpoint() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test\",\"email\":\"test@test.com\",\"password\":\"password123\",\"rol\":\"CLIENTE\",\"apellido\":\"Test\"}"))
                .andExpect(status().isCreated());  // ✅ 201 Created, no 200 OK
    }

    @Test
    void shouldAllowPublicAccessToProductosGet() throws Exception {
        mockMvc.perform(get("/api/celulares"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldAllowPublicAccessToAccesoriosGet() throws Exception {
        mockMvc.perform(get("/api/accesorios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToCreateProducto() throws Exception {
        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test Celular\",\"precio\":100.0,\"stock\":10,\"marca\":\"Test\",\"modelo\":\"Test\",\"almacenamientoGB\":128,\"ramGB\":8,\"procesador\":\"Test\",\"pantallaPulgadas\":6.0,\"bateriaMAh\":4000,\"sistemaOperativo\":\"Android\"}"))
                .andExpect(status().isCreated());  // ✅ 201 Created, no 200 OK
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void shouldAllowEmpleadoToCreateProducto() throws Exception {
        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test Celular\",\"precio\":100.0,\"stock\":10,\"marca\":\"Test\",\"modelo\":\"Test\",\"almacenamientoGB\":128,\"ramGB\":8,\"procesador\":\"Test\",\"pantallaPulgadas\":6.0,\"bateriaMAh\":4000,\"sistemaOperativo\":\"Android\"}"))
                .andExpect(status().isCreated());  // ✅ 201 Created, no 200 OK
    }

    @Test
    void shouldDenyAccessToProductoPostWithoutAuth() throws Exception {
        mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Test Celular\",\"precio\":100.0,\"stock\":10,\"marca\":\"Test\",\"modelo\":\"Test\",\"almacenamientoGB\":128,\"ramGB\":8,\"procesador\":\"Test\",\"pantallaPulgadas\":6.0,\"bateriaMAh\":4000,\"sistemaOperativo\":\"Android\"}"))
                .andExpect(status().isForbidden());  // ✅ 403 Forbidden, no 401 Unauthorized
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToAccessClientesEndpoint() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void shouldDenyEmpleadoToAccessClientesEndpoint() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void shouldDenyClienteToAccessClientesEndpoint() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "CLIENTE", username = "cliente@test.com")
    void shouldAllowClienteToAccessMisVentasEndpoint() throws Exception {
        // ✅ Primero registrar un usuario con ese email
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Cliente\",\"email\":\"cliente@test.com\",\"password\":\"password123\",\"rol\":\"CLIENTE\",\"apellido\":\"Test\"}"))
                .andExpect(status().isCreated());

        // ✅ Luego acceder a mis-ventas
        mockMvc.perform(get("/api/ventas/mis-ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void shouldAllowEmpleadoToAccessVentasEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToAccessVentasEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void shouldDenyClienteToAccessVentasEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToAccessVentasReportesEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas/reportes/por-estado"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "EMPLEADO")
    void shouldAllowEmpleadoToAccessVentasReportesEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas/reportes/por-estado"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void shouldDenyClienteToAccessVentasReportesEndpoint() throws Exception {
        mockMvc.perform(get("/api/ventas/reportes/por-estado"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void shouldAllowAdminToDeleteProducto() throws Exception {
        // ✅ Primero crear un producto
        String productJson = "{\"nombre\":\"Test Celular\",\"precio\":100.0,\"stock\":10,\"marca\":\"Test\",\"modelo\":\"Test\",\"almacenamientoGB\":128,\"ramGB\":8,\"procesador\":\"Test\",\"pantallaPulgadas\":6.0,\"bateriaMAh\":4000,\"sistemaOperativo\":\"Android\"}";

        MvcResult result = mockMvc.perform(post("/api/celulares")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productJson))
                .andExpect(status().isCreated())
                .andReturn();

        // ✅ Extraer el ID del producto creado
        String response = result.getResponse().getContentAsString();
        // Parsear el JSON para obtener el ID (usando Jackson)
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response);
        Long id = node.get("idProducto").asLong();

        // ✅ Luego eliminarlo
        mockMvc.perform(delete("/api/celulares/" + id))
                .andExpect(status().isNoContent());  // ✅ 204 No Content
    }

    @Test
    @WithMockUser(roles = "CLIENTE")
    void shouldDenyClienteToDeleteProducto() throws Exception {
        mockMvc.perform(delete("/api/celulares/1"))
                .andExpect(status().isForbidden());
    }
}
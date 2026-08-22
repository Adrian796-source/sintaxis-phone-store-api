package com.adrian.sintaxis.config;

import com.adrian.sintaxis.model.Usuario;
import com.adrian.sintaxis.model.Rol;
import com.adrian.sintaxis.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    // Lee las variables de entorno (Render) o usa valores por defecto si estás en local
    @Value("${ADMIN_EMAIL:admin@localhost.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:admin123}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        // Si el admin no existe, lo creamos
        if (!usuarioRepository.existsByEmail(adminEmail)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRol(Rol.ADMIN);
            admin.setActivo(true);
            usuarioRepository.save(admin);
            System.out.println("✅ Usuario ADMIN creado exitosamente.");
        } else {
            System.out.println("ℹ️ Usuario ADMIN ya existía, no se creó uno nuevo.");
        }
    }
}
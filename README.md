# 📱 Sintaxis Phone Store API

API REST completa para la gestión de una tienda de celulares y accesorios. Desarrollada con Spring Boot y Spring Security, implementa autenticación JWT, gestión de ventas, puntos VIP y reportes de rendimiento.

## 🚀 Tecnologías
- Java 17
- Spring Boot 3.x
- Spring Security + JWT
- JPA / Hibernate
- PostgreSQL
- Docker + Docker Compose
- Swagger / OpenAPI (Documentación)
- CORS (Configuración para frontend)
- Postman (Pruebas de endpoints)
- **JUnit 5 + Mockito (Testing)**

## 📌 Características principales
### 🔐 Autenticación y Seguridad
- Registro e inicio de sesión con JWT.
- Logout con blacklist de tokens.
- Roles y permisos con Spring Security.
- Configuración de CORS para comunicação con frontend.

### 📦 Gestión de Negocio
- CRUD de clientes.
- CRUD de celulares y accesorios.
- Gestión de ventas y reportes de rendimiento.
- Sistema de puntos VIP para clientes frecuentes.
- Asociación entre usuarios y clientes.

### 🧱 Arquitectura
- Capas bien definidas: Controller → Service → Repository.
- Uso de DTOs para transferencia de datos.
- Swagger para documentación interactiva de la API.

## 🧪 Testing
La aplicación cuenta con pruebas unitarias y de integración para garantizar la robustez del código:
- **Framework:** JUnit 5.
- **Mocking:** Mockito (simulación de dependencias de servicios y repositorios).
- **Cobertura:** Se testean los servicios principales (lógica de negocio), validando el correcto funcionamiento de los CRUD, la lógica de ventas y la seguridad.

> *Para ejecutar los tests localmente:*
> ```bash
> mvn test
> ```

## 🔒 Seguridad
La API está protegida con JWT. Los endpoints de escritura (POST, PUT, DELETE) son privados y requieren un token con rol **ADMIN** o **EMPLEADO**.

> **Cómo probar la app en local:**
> 1. Ejecuta el proyecto con el perfil de prueba (H2) para que arranque más rápido.
> 2. Registra un usuario en `/api/auth/register` (este usuario tendrá rol CLIENTE y solo podrá ver los productos).
> 3. Para probar las funcionalidades de Admin (crear, editar, borrar), el usuario Admin se crea automáticamente al arrancar la app utilizando las variables de entorno `ADMIN_EMAIL` y `ADMIN_PASSWORD`.
>
> **Para el recruiter:** Si bajas este proyecto y lo ejecutas localmente, el Admin se creará con los valores por defecto. En producción, estas variables están configuradas en Render de forma segura, sin exponer la contraseña en GitHub.

## 🐳 Despliegue
### Contenerización
- Contenerización con Docker y Docker Compose.
- Variables de entorno para configuración segura.

### Producción (Live en Render)
La API está desplegada en la nube utilizando **Render**:
- **URL de Producción:** `https://sintaxis-phone-store-api.onrender.com`
- **Base de Datos:** PostgreSQL gestionado en la nube (Render).
- **Infraestructura:** Imagen Docker construida y alojada por Render (con conexión segura SSL a la base de datos).
- *Nota: Al ser un servicio gratuito, el primer acceso puede tardar unos segundos mientras la instancia se "despierta" (cold start).*

## 🛠️ Configuración y uso
### 🔧 Requisitos
- Java 17
- Maven
- Docker (opcional)
- PostgreSQL

### ▶️ Ejecutar con Docker
```bash
docker-compose up -d
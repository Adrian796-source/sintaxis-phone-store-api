# ===== IMAGEN BASE =====
FROM eclipse-temurin:17-jdk-alpine

# ===== INFORMACIÓN DEL MANTENEDOR =====
LABEL maintainer="adrianbastaloguzzo@gmail.com"
LABEL version="1.0.0"
LABEL description="Sintaxis Phone Store API"

# ===== CREAR DIRECTORIO DE TRABAJO =====
WORKDIR /app

# ===== CREAR DIRECTORIO PARA IMÁGENES =====
RUN mkdir -p /app/uploads

# ===== COPIAR EL JAR =====
# El JAR se copia al construir la imagen
COPY target/sintaxis-0.0.1-SNAPSHOT.jar app.jar

# ===== EXPONER PUERTO =====
EXPOSE 8080

# ===== COMANDO DE EJECUCIÓN =====
# Las variables de entorno se pasan al ejecutar el contenedor
ENTRYPOINT ["java", "-jar", "app.jar"]



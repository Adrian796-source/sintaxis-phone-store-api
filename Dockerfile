# ===== IMAGEN BASE =====
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de Maven
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

# Descargar dependencias
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# Copiar código fuente y compilar
COPY src src
RUN ./mvnw clean package -DskipTests

# ===== SEGUNDA ETAPA: Imagen final =====
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="adrianbastaloguzzo@gmail.com"
LABEL version="1.0.0"
LABEL description="Sintaxis Phone Store API"

WORKDIR /app

# Crear directorio para imágenes
RUN mkdir -p /app/uploads

# Copiar el JAR desde la etapa de construcción
COPY --from=builder /app/target/sintaxis-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
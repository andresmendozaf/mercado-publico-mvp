# ==========================================
# ETAPA 1: Construcción (Build Stage)
# ==========================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar configuración Maven para cachear librerías
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar el código fuente Java y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ==========================================
# ETAPA 2: Ejecución Segura (Runtime Stage)
# ==========================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 1. Crear un grupo y usuario no-root exclusivo para la app
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# 2. Copiar el .jar desde el builder
COPY --from=builder /app/target/*.jar app.jar

# 3. Asignar la propiedad de los archivos en /app al nuevo usuario
RUN chown -R appuser:appgroup /app

# 4. Cambiar de contexto al usuario sin privilegios
USER appuser

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Comando de inicio
ENTRYPOINT ["java", "-jar", "app.jar"]
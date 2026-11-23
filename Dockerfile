# ============================================================
# Dockerfile - Backend Produccion (Spring Boot 4.2.0 + Java 21)
# Desplegar en Render Cloud conectando el repositorio Git
# Variables de entorno necesarias (configurarlas en Render):
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#   CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
#   SERVER_PORT (opcional, por defecto 8080)
# ============================================================

# ---------- Etapa 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copiar solo pom.xml primero para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ---------- Etapa 2: Run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar el jar generado en la etapa de build
COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

# Puerto de la aplicación
EXPOSE 8080

# Variables de entorno con valores por defecto (se sobrescriben en Render)
ENV SERVER_PORT=8080
ENV SPRING_DATASOURCE_URL=""
ENV SPRING_DATASOURCE_USERNAME=""
ENV SPRING_DATASOURCE_PASSWORD=""
ENV CLOUDINARY_CLOUD_NAME=""
ENV CLOUDINARY_API_KEY=""
ENV CLOUDINARY_API_SECRET=""

# Punto de entrada
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

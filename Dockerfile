# ============================================================
# Dockerfile - Backend Produccion (Spring Boot 4.2.0 + Java 21)
# Variables de entorno a configurar en Render:
#   SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD
#   APP_JWT_SECRET
#   CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET
# ============================================================

# ---------- Etapa 1: Build ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B -q

COPY src ./src
RUN mvn clean package -DskipTests -B -q

# ---------- Etapa 2: Run ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/demo-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Limite de RAM seguro para el plan Free de Render (512 MB)
ENTRYPOINT ["java", "-Xmx384m", "-jar", "/app/app.jar"]

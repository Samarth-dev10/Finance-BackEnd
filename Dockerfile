# syntax=docker/dockerfile:1

# Build stage
FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Cache dependencies first to speed up rebuilds
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests clean package

# Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Run as non-root user
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
USER spring:spring

ENTRYPOINT ["java", "-jar", "/app/app.jar"]


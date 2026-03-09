# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cached as a separate layer)
RUN mvn dependency:go-offline --batch-mode
COPY src ./src
RUN mvn package -DskipTests --batch-mode

# ── Stage 2: Run ──────────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/java-app-1.0.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]

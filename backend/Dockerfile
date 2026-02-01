# =============================================================================
# Aletheia AI Backend — Multi-stage Dockerfile
# =============================================================================
# Build: ./mvnw -DskipTests package
# Run: java -jar /app/app.jar
# Env: SPRING_DATASOURCE_URL, AI_ALETHEIA_SIGNING_KEY_PATH, AI_ALETHEIA_TSA_MODE,
#      AI_ALETHEIA_TSA_URL, OPENAI_API_KEY
# Signing key: mount at runtime, e.g. -v /host/ai.key:/app/ai.key
# =============================================================================

# --- Stage 1: Build ---
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Copy Maven wrapper and pom
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Download dependencies (cached if pom unchanged)
RUN ./mvnw dependency:resolve -B

# Copy source and build (skip tests for faster image build)
COPY src src
RUN ./mvnw -DskipTests package -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built jar from builder
COPY --from=builder /build/target/*.jar app.jar

EXPOSE 8080

# Environment variables (override at runtime)
ENV SPRING_DATASOURCE_URL=""
ENV AI_ALETHEIA_SIGNING_KEY_PATH=""
ENV AI_ALETHEIA_TSA_MODE=""
ENV AI_ALETHEIA_TSA_URL=""
ENV OPENAI_API_KEY=""

ENTRYPOINT ["java", "-jar", "app.jar"]

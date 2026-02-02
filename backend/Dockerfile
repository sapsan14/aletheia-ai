# =============================================================================
# Aletheia AI Backend — Multi-stage Dockerfile
# =============================================================================
# Build: mvn -DskipTests package -Pverifier (main app + offline verifier JAR)
# Run: java -jar /app/app.jar
# Env: SPRING_DATASOURCE_URL, AI_ALETHEIA_SIGNING_KEY_PATH, AI_ALETHEIA_TSA_MODE,
#      AI_ALETHEIA_TSA_URL, OPENAI_API_KEY, AI_ALETHEIA_VERIFIER_JAR_PATH
# Signing key: mount at runtime, e.g. -v /host/ai.key:/app/ai.key
# =============================================================================

# --- Stage 1: Build ---
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY pom.xml .

# Download dependencies (cached if pom unchanged)
RUN mvn dependency:resolve -B

# Copy source and build main app (fat jar), then verifier JAR (-Pverifier disables repackage, so build in two steps)
COPY src src
RUN mvn -DskipTests package -B && \
    cp /build/target/backend-0.0.1-SNAPSHOT.jar /build/target/app-fat.jar && \
    mvn package -Pverifier -DskipTests -B

# --- Stage 2: Runtime ---
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy main app fat jar (saved before -Pverifier overwrote it) and offline verifier JAR (for GET /api/ai/verifier)
COPY --from=builder /build/target/app-fat.jar app.jar
COPY --from=builder /build/target/aletheia-verifier.jar /app/aletheia-verifier.jar

EXPOSE 8080

# Env vars (optional): pass at runtime with -e to override application.properties defaults.
# See header comment for supported variables. Do not set ENV here to empty string,
# or Spring would treat them as set and ignore defaults (H2, real TSA, etc.).

ENTRYPOINT ["java", "-jar", "app.jar"]

# ---- Build stage ----
FROM maven:3.9.6-eclipse-temurin-25 AS builder
WORKDIR /workspace

# Copy maven files first to use layer caching
COPY infraimatic/pom.xml infraimatic/pom.xml
COPY infraimatic/*.xml infraimatic/ 2>/dev/null || true
# Copy module pom files so dependency resolution can be cached
COPY infraimatic/*/pom.xml infraimatic/
# Copy the whole project
COPY . .

# Build the project (skip tests here if CI already ran tests)
RUN mvn -B -f infraimatic/pom.xml -DskipTests package -T 1C

# ---- Runtime stage ----
FROM eclipse-temurin:25-jdk-jammy
ARG JAR_FILE=infraimatic/api/target/*.jar
# Create app user for security
RUN useradd --create-home --shell /bin/bash appuser
WORKDIR /app

# Copy the fat jar produced by the API module
COPY --from=builder /workspace/${JAR_FILE} /app/app.jar

# Expose port (default Spring Boot port)
EXPOSE 8080

# Recommended JVM options — tune per environment
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -Djava.security.egd=file:/dev/./urandom"

# Run as non-root
USER appuser

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]

# Build stage
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

# Copy gradle files first for better caching
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install Tailscale
RUN apt-get update && \
    apt-get install -y curl iptables && \
    curl -fsSL https://tailscale.com/install.sh | sh && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Create directories for Tailscale
RUN mkdir -p /var/lib/tailscale /var/run/tailscale

COPY --from=builder /app/build/libs/*.jar app.jar
COPY docker/start.sh /app/start.sh
RUN chmod +x /app/start.sh

EXPOSE 8080

# Environment variables (set at runtime)
ENV SPRING_PROFILES_ACTIVE=docker
ENV JWT_SECRET=""
ENV DB_URL=""
ENV DB_USERNAME=""
ENV DB_PASSWORD=""
ENV FIREBASE_ADMIN_JSON_BASE64=""
ENV TAILSCALE_ENABLED="false"
ENV TAILSCALE_AUTHKEY=""
ENV ENVIRONMENT="staging"

ENTRYPOINT ["/app/start.sh"]

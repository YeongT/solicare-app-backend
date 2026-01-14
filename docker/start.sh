#!/bin/bash

# Tailscale 연결 (환경변수로 제어)
if [ "$TAILSCALE_ENABLED" = "true" ] && [ -n "$TAILSCALE_AUTHKEY" ]; then
    echo "[INFO] Starting Tailscale..."
    tailscaled --state=/var/lib/tailscale/tailscaled.state --socket=/var/run/tailscale/tailscaled.sock &
    sleep 2
    tailscale up --authkey="$TAILSCALE_AUTHKEY" --hostname="solicare-${ENVIRONMENT:-staging}"
    echo "[INFO] Tailscale connected!"
fi

# Spring Boot 실행
echo "[INFO] Starting Spring Boot application..."
exec java -jar /app/app.jar

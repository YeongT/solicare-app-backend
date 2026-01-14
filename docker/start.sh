#!/bin/bash

# Tailscale 연결 (환경변수로 제어)
if [ "$TAILSCALE_ENABLED" = "true" ] && [ -n "$TAILSCALE_AUTHKEY" ]; then
    echo "[INFO] Starting Tailscale (userspace mode)..."
    mkdir -p /tmp/tailscale
    tailscaled --tun=userspace-networking --state=/tmp/tailscale/tailscaled.state --socket=/tmp/tailscale/tailscaled.sock &
    sleep 3
    tailscale --socket=/tmp/tailscale/tailscaled.sock up --authkey="$TAILSCALE_AUTHKEY" --hostname="solicare-${ENVIRONMENT:-staging}"

    # Userspace 모드에서는 SOCKS5 프록시 사용
    export ALL_PROXY=socks5h://localhost:1055
    export HTTP_PROXY=socks5h://localhost:1055
    echo "[INFO] Tailscale connected! (proxy: localhost:1055)"
fi

# Spring Boot 실행
echo "[INFO] Starting Spring Boot application..."
exec java -jar /app/app.jar

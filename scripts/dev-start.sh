#!/bin/bash
set -e

echo "🚀 Starting DexAPI development environment..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Start Docker services
echo "📦 Starting Docker services..."
docker-compose up -d postgres redis

# Wait for services with health check
echo "⏳ Waiting for services to be ready..."
timeout=30
while [ $timeout -gt 0 ]; do
    if docker-compose exec -T postgres pg_isready -U dexapi > /dev/null 2>&1; then
        echo "✅ PostgreSQL is ready"
        break
    fi
    echo "⏳ Waiting for PostgreSQL... ($timeout seconds left)"
    sleep 2
    timeout=$((timeout-2))
done

if [ $timeout -le 0 ]; then
    echo "❌ PostgreSQL failed to start in time"
    exit 1
fi

echo "🌱 Docker services are ready!"
echo "💡 You can now start your Spring Boot application in IntelliJ"
echo "💡 Or run: ./mvnw spring-boot:run"

# Keep script running to show logs (optional)
if [ "$1" = "--logs" ]; then
    echo "📋 Showing Docker logs (Ctrl+C to stop):"
    docker-compose logs -f postgres redis
fi

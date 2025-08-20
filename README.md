# DexAPI

A comprehensive RESTful API providing detailed Pokémon data for developers and applications. Built and maintained by the Dexellent organization.

## 🚀 Overview

DexAPI serves as the core data source for Pokémon information, powering applications like [Dexplorer](https://github.com/dexellent/dexplorer) and available for public use. Access detailed information about Pokémon, moves, types, abilities, generations, and more.

## 📋 Features

- **Complete Pokémon Database** - All Pokémon across generations *(coming soon)*
- **Moves & Abilities** - Comprehensive move sets and ability data *(coming soon)*
- **Type System** - Type effectiveness and relationships *(coming soon)*
- **Regional Pokédex** - Region-specific Pokédex entries *(coming soon)*
- **Generation Data** - Game generation information *(coming soon)*
- **Team Building Support** - Data optimized for team composition tools *(coming soon)*
- **Fast & Reliable** - Optimized for performance *(coming soon)*
- **Public Access** - Free to use with rate limiting *(coming soon)*

## 🔗 Base URL

```
https://api.dexellent.dev/v1
```

## 📚 Quick Start

### Get a Pokémon *(coming soon)*
```bash
curl https://api.dexellent.dev/v1/pokemon/pikachu
```

### Get a Move *(coming soon)*
```bash
curl https://api.dexellent.dev/v1/moves/thunderbolt
```

### Get Type Information *(coming soon)*
```bash
curl https://api.dexellent.dev/v1/types/electric
```

## 🛠 API Endpoints *(coming soon)*

| Endpoint | Description | Example |
|----------|-------------|---------|
| `GET /pokemon/{id\|name}` | Get Pokémon details | `/pokemon/25` or `/pokemon/pikachu` |
| `GET /pokemon` | List Pokémon with filters | `/pokemon?type=electric&generation=1` |
| `GET /moves/{id\|name}` | Get move details | `/moves/thunderbolt` |
| `GET /moves` | List moves with filters | `/moves?type=electric&power=90` |
| `GET /types/{id\|name}` | Get type information | `/types/electric` |
| `GET /types` | List all types | `/types` |
| `GET /abilities/{id\|name}` | Get ability details | `/abilities/static` |
| `GET /generations/{id}` | Get generation info | `/generations/1` |
| `GET /pokedex/{region}` | Get regional Pokédex | `/pokedex/kanto` |

## 📖 Documentation *(coming soon)*

- **Full API Documentation**: [docs.dexellent.dev](https://docs.dexellent.dev)
- **Interactive API Explorer**: [api.dexellent.dev/docs](https://api.dexellent.dev/docs)
- **Postman Collection**: [Download](https://api.dexellent.dev/postman)

## 🔑 Authentication *(coming soon)*

Currently, DexAPI is publicly accessible without authentication. Rate limiting applies:

- **Free Tier**: 1000 requests/hour
- **Registered Users**: 5000 requests/hour (coming soon)

## 📊 Response Format *(coming soon)*

All responses follow a consistent JSON structure:

```json
{
  "success": true,
  "data": {
    // Resource data
  },
  "meta": {
    "timestamp": "2025-08-20T15:00:00Z",
    "version": "1.0.0"
  }
}
```

## 🚦 Rate Limiting *(coming soon)*

Rate limit information is included in response headers:

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1692547200
```

## 🛠 Tech Stack

- **Runtime**: Java 21
- **Framework**: Spring Boot 3.x
- **Database**: PostgreSQL
- **Cache**: Redis
- **Documentation**: SpringDoc OpenAPI
- **Build Tool**: Maven
- **Hosting**: [Your hosting platform]

## 🚀 Development

### Prerequisites
- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- Redis 7+ (optional, for caching)

### Running Locally *(coming soon)*

```bash
# Clone the repository
git clone https://github.com/dexellent/dexapi.git
cd dexapi

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/dexapi-1.0.0.jar
```

### Environment Variables *(coming soon)*

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/dexapi
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Redis (optional)
SPRING_DATA_REDIS_HOST=localhost
SPRING_DATA_REDIS_PORT=6379

# API Configuration
API_RATE_LIMIT_REQUESTS_PER_HOUR=1000
```

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🐛 Issues & Support

- **Bug Reports**: [GitHub Issues](https://github.com/dexellent/dexapi/issues)
- **Feature Requests**: [GitHub Discussions](https://github.com/dexellent/dexapi/discussions)
- **Support**: [support@dexellent.dev](mailto:support@dexellent.dev)

## 🔗 Related Projects

- **[Dexplorer](https://github.com/dexellent/dexplorer)** - Pokédex app powered by DexAPI
- **[Dexellent Organization](https://github.com/dexellent)** - All our Pokémon-related projects

## 📈 Status

- **API Status**: [status.dexellent.dev](https://status.dexellent.dev)
- **Version**: 1.0.0
- **Last Updated**: August 2025

---

Made with ❤️ by the [Dexellent](https://github.com/dexellent) team
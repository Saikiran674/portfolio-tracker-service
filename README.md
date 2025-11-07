# Portfolio Tracker Service

Java 17 + Spring Boot backend service for managing investment portfolios.
Includes JWT-based auth, PostgreSQL, scheduled mock price refresh, and Docker setup.

## Run locally (requires Postgres)

1. Start Postgres (or use docker-compose):
```bash
docker-compose up db -d
```

2. Run the app:
```bash
mvn spring-boot:run
```

## Run everything with Docker Compose

```bash
docker-compose up --build
```

App will be available at http://localhost:8080

## Sample endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/portfolios` (requires Bearer token)
- `POST /api/portfolios`
- `POST /api/portfolios/{portfolioId}/holdings`
